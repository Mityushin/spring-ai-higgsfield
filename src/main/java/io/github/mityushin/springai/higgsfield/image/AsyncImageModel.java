package io.github.mityushin.springai.higgsfield.image;

import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageResponse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;

public interface AsyncImageModel extends ImageModel {

	default byte[] downloadContent(ImageResponse imageResponse) {
		if (imageResponse.getResults().isEmpty()) {
			throw new IllegalArgumentException("ImageResponse contains no results");
		}
		var url = imageResponse.getResults().getFirst().getOutput().getUrl();
		return downloadContent(url);
	}

	default byte[] downloadContent(String url) {
		if (url == null || url.isBlank()) {
			throw new IllegalStateException("No URL available in image response for download");
		}
		try {
			var imageUrl = URI.create(url).toURL();
			try (var inputStream = imageUrl.openStream()) {
				return inputStream.readAllBytes();
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException("Failed to download image from: " + url, e);
		}
	}

	ImageResponse pollTaskResult(String taskId);

}
