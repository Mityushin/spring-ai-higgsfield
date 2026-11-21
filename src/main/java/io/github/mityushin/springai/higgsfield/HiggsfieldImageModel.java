package io.github.mityushin.springai.higgsfield;

import io.github.mityushin.higgsfield.HiggsfieldClient;
import io.github.mityushin.higgsfield.PollingOptions;
import org.springframework.ai.image.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Blocking Spring AI image adapter; the underlying request uses asynchronous Higgsfield
 * jobs.
 */
public final class HiggsfieldImageModel implements ImageModel {

	private final HiggsfieldClient client;

	private final PollingOptions polling;

	public HiggsfieldImageModel(HiggsfieldClient client, PollingOptions polling) {
		this.client = Objects.requireNonNull(client);
		this.polling = Objects.requireNonNull(polling);
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
		var result = handle.waitForResult(polling);
		if (!result.isCompleted())
			throw new IllegalStateException(
					"Higgsfield request " + handle.requestId() + " ended with status " + result.status());
		var images = result.body().path("images");
		if (!images.isArray() || images.isEmpty())
			throw new IllegalStateException("Completed Higgsfield request has no images: " + handle.requestId());
		var generations = new ArrayList<ImageGeneration>();
		images.forEach(image -> {
			String url = image.path("url").asText(null);
			if (url == null || url.isBlank())
				throw new IllegalStateException("Higgsfield image has no URL: " + handle.requestId());
			generations.add(new ImageGeneration(new Image(url, null)));
		});
		return new ImageResponse(generations);
	}

}
