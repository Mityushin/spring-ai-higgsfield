package io.github.mityushin.higgsfield;

import io.github.mityushin.springai.higgsfield.*;
import org.junit.jupiter.api.Test;
import io.github.mityushin.springai.higgsfield.video.*;
import org.springframework.ai.image.*;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import javax.net.ssl.*;
import static org.junit.jupiter.api.Assertions.*;

class HiggsfieldModelsTest {
    @Test void seedanceSubmitAndAllVideoStates() {
        AtomicReference<String> state = new AtomicReference<>("queued");
        FakeHttp http = new FakeHttp(request -> {
            if (request.uri().getPath().equals("/bytedance/seedance-2.0/text-to-video")) {
                assertEquals("POST", request.method());
                assertEquals("request-123", request.headers().firstValue("Idempotency-Key").orElseThrow());
                String body = readBody(request);
                assertTrue(body.contains("\"duration\":5"));
                assertTrue(body.contains("\"prompt\":\"sunset\""));
                assertTrue(body.contains("\"aspect_ratio\":\"16:9\""));
                return "{\"request_id\":\"123\",\"status_url\":\"https://api.example/requests/123/status\",\"cancel_url\":\"https://api.example/requests/123/cancel\"}";
            }
            return switch (state.get()) {
                case "completed" -> "{\"status\":\"completed\",\"video\":{\"url\":\"https://cdn.example/video.mp4\"}}";
                default -> "{\"status\":\"" + state.get() + "\"}";
            };
        });
        try (var client = client(http)) {
            var model = new HiggsfieldVideoModel(client);
            var options = new HiggsfieldVideoOptions("bytedance/seedance-2.0/text-to-video", Map.of("duration", 5, "aspect_ratio", "16:9"), new SubmitOptions(null, "request-123"));
            assertEquals("123", model.call(new VideoPrompt("sunset", options)).getResult().getOutput().getTaskId());
            assertEquals(VideoJobStatus.IN_PROGRESS, model.pollJobResult("123").getStatus());
            state.set("completed");
            assertEquals("https://cdn.example/video.mp4", model.pollJobResult("123").getUrl());
            state.set("failed");
            assertEquals(VideoJobStatus.ERROR, model.pollJobResult("123").getStatus());
            state.set("new_status");
            assertThrows(IllegalStateException.class, () -> model.pollJobResult("123"));
        }
    }
    @Test void imageSubmitAndPollStates() {
        AtomicReference<String> state = new AtomicReference<>("queued");
        FakeHttp http = new FakeHttp(request -> request.method().equals("POST")
            ? "{\"request_id\":\"abc\",\"status_url\":\"https://api.example/requests/abc/status\",\"cancel_url\":\"https://api.example/requests/abc/cancel\"}"
            : state.get().equals("completed") ? "{\"status\":\"completed\",\"images\":[{\"url\":\"https://cdn.example/1.png\"},{\"url\":\"https://cdn.example/2.png\"}]}"
            : "{\"status\":\"" + state.get() + "\"}");
        try (var client = client(http)) {
            var model = new HiggsfieldAsyncImageModel(client);
            var options = new HiggsfieldImageOptions("marketing-studio/image", Map.of());
            assertEquals("abc", model.call(new ImagePrompt("sunset", options)).getMetadata().get("taskId"));
            assertTrue(model.pollTaskResult("abc").getResults().isEmpty());
            state.set("completed");
            assertEquals(2, model.pollTaskResult("abc").getResults().size());
            state.set("failed");
            assertTrue(model.pollTaskResult("abc").getMetadata().containsKey("error"));
        }
    }
    private static HiggsfieldClient client(HttpClient http) { return HiggsfieldClient.builder().baseUrl(URI.create("https://api.example/")).credentials("id:secret").httpClient(http).build(); }
    private static String readBody(HttpRequest request) {
        var result = new CompletableFuture<String>();
        var bytes = new java.io.ByteArrayOutputStream();
        request.bodyPublisher().orElseThrow().subscribe(new java.util.concurrent.Flow.Subscriber<java.nio.ByteBuffer>() {
            public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) { subscription.request(Long.MAX_VALUE); }
            public void onNext(java.nio.ByteBuffer chunk) { byte[] data = new byte[chunk.remaining()]; chunk.get(data); bytes.writeBytes(data); }
            public void onError(Throwable error) { result.completeExceptionally(error); }
            public void onComplete() { result.complete(bytes.toString(java.nio.charset.StandardCharsets.UTF_8)); }
        });
        return result.join();
    }
    private static final class FakeHttp extends HttpClient {
        private final Function<HttpRequest, String> response;
        FakeHttp(Function<HttpRequest, String> response) { this.response = response; }
        @Override public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest r, HttpResponse.BodyHandler<T> h) {
            @SuppressWarnings("unchecked") T body = (T) response.apply(r);
            HttpResponse<T> result = new HttpResponse<>() {
                public int statusCode() { return 200; } public HttpRequest request() { return r; }
                public Optional<HttpResponse<T>> previousResponse() { return Optional.empty(); }
                public HttpHeaders headers() { return HttpHeaders.of(Map.of(), (a,b) -> true); }
                public T body() { return body; } public Optional<SSLSession> sslSession() { return Optional.empty(); }
                public URI uri() { return r.uri(); } public Version version() { return Version.HTTP_1_1; }
            };
            return CompletableFuture.completedFuture(result);
        }
        @Override public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest r, HttpResponse.BodyHandler<T> h, HttpResponse.PushPromiseHandler<T> p) { return sendAsync(r,h); }
        @Override public <T> HttpResponse<T> send(HttpRequest r, HttpResponse.BodyHandler<T> h) { throw new UnsupportedOperationException(); }
        public Optional<CookieHandler> cookieHandler() { return Optional.empty(); } public Optional<Duration> connectTimeout() { return Optional.empty(); }
        public Redirect followRedirects() { return Redirect.NEVER; } public Optional<ProxySelector> proxy() { return Optional.empty(); }
        public SSLContext sslContext() { return null; } public SSLParameters sslParameters() { return null; }
        public Optional<Authenticator> authenticator() { return Optional.empty(); } public Version version() { return Version.HTTP_1_1; }
        public Optional<Executor> executor() { return Optional.empty(); }
    }
}
