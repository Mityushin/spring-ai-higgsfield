package io.github.mityushin.springai.higgsfield;

import io.github.mityushin.higgsfield.HiggsfieldClient;
import io.github.mityushin.higgsfield.RequestStatus;
import io.github.mityushin.springai.higgsfield.video.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Video model for Higgsfield endpoints; call submits and pollJobResult checks one status.
 */
public final class HiggsfieldVideoModel implements VideoModel {

	private final HiggsfieldClient client;

	public HiggsfieldVideoModel(HiggsfieldClient client) {
		this.client = Objects.requireNonNull(client);
	}

	@Override
	public VideoResponse call(VideoPrompt prompt) {
		if (!(prompt.getOptions() instanceof HiggsfieldVideoOptions options))
			throw new IllegalArgumentException("HiggsfieldVideoOptions are required");
		var input = new LinkedHashMap<String, Object>(options.getParameters());
		if (prompt.getInstructions() != null && !prompt.getInstructions().isBlank())
			input.put("prompt", prompt.getInstructions());
		var handle = client.submit(options.getModel(), input, options.getSubmitOptions());
		return new VideoResponse(new VideoGeneration(Video.ofTaskId(handle.requestId())));
	}

	@Override
	public Video pollJobResult(String taskId) {
		RequestStatus status = client.status(taskId);
		if (!status.isKnown())
			throw new IllegalStateException("Unknown Higgsfield status: " + status.status());
		if (!status.isTerminal())
			return Video.ofTaskId(taskId);
		if (!status.isCompleted())
			return Video.ofFailedTaskId(taskId, "Higgsfield generation " + status.status());
		String url = status.body().path("video").path("url").asText(null);
		if (url == null || url.isBlank())
			throw new IllegalStateException("Completed Higgsfield request has no video URL: " + taskId);
		return Video.ofUrl(url);
	}

	@Override
	public byte[] downloadContent(Video video) {
		if (video.getUrl() == null)
			throw new IllegalArgumentException("Video has no URL");
		try {
			var request = HttpRequest.newBuilder(URI.create(video.getUrl()))
				.timeout(Duration.ofMinutes(2))
				.GET()
				.build();
			var response = HttpClient.newBuilder()
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build()
				.send(request, HttpResponse.BodyHandlers.ofByteArray());
			if (response.statusCode() < 200 || response.statusCode() >= 300)
				throw new IllegalStateException("Video download failed with HTTP " + response.statusCode());
			return response.body();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Video download interrupted", e);
		}
		catch (IOException e) {
			throw new IllegalStateException("Video download failed", e);
		}
	}

}
