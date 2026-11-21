package io.github.mityushin.springai.higgsfield;

import io.github.mityushin.higgsfield.HiggsfieldClient;
import org.springframework.ai.image.*;
import io.github.mityushin.springai.higgsfield.image.AsyncImageModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/** Submit and resume image jobs through the asynchronous image contract. */
public final class HiggsfieldAsyncImageModel implements AsyncImageModel {

	private final HiggsfieldClient client;

	public HiggsfieldAsyncImageModel(HiggsfieldClient client) {
		this.client = Objects.requireNonNull(client);
	}

	@Override
	public ImageResponse call(ImagePrompt prompt) {
		if (!(prompt.getOptions() instanceof HiggsfieldImageOptions options))
			throw new IllegalArgumentException("HiggsfieldImageOptions are required");
		if (prompt.getInstructions().size() != 1)
			throw new IllegalArgumentException("Exactly one image prompt is supported");
		var message = prompt.getInstructions().getFirst();
		if (!message.getMedia().isEmpty() || message.getWeight() != null)
			throw new IllegalArgumentException(
					"Pass model-specific media as explicit parameters; prompt weights are not supported");
		var input = new LinkedHashMap<String, Object>(options.getParameters());
		input.put("prompt", message.getText());
		var handle = client.submit(options.getModel(), input, options.getSubmitOptions());
		var metadata = new ImageResponseMetadata();
		metadata.put("taskId", handle.requestId());
		return new ImageResponse(List.of(), metadata);
	}

	@Override
	public ImageResponse pollTaskResult(String taskId) {
		var status = client.status(taskId);
		var metadata = new ImageResponseMetadata();
		if (!status.isKnown())
			throw new IllegalStateException("Unknown Higgsfield status: " + status.status());
		if (!status.isTerminal())
			return new ImageResponse(List.of(), metadata);
		if (!status.isCompleted()) {
			metadata.put("error", "Higgsfield generation " + status.status());
			return new ImageResponse(List.of(), metadata);
		}
		var images = status.body().path("images");
		if (!images.isArray() || images.isEmpty())
			throw new IllegalStateException("Completed Higgsfield request has no images: " + taskId);
		var generations = new ArrayList<ImageGeneration>();
		images.forEach(image -> {
			String url = image.path("url").asText(null);
			if (url == null || url.isBlank())
				throw new IllegalStateException("Higgsfield image has no URL: " + taskId);
			generations.add(new ImageGeneration(new Image(url, null)));
		});
		return new ImageResponse(generations, metadata);
	}

}
