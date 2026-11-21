package io.github.mityushin.springai.higgsfield.video;

import org.springframework.ai.model.Model;

import java.nio.ByteBuffer;

@FunctionalInterface
public interface VideoModel extends Model<VideoPrompt, VideoResponse> {

	@Override
	VideoResponse call(VideoPrompt prompt);

	default byte[] downloadContent(Video video) {
		throw new UnsupportedOperationException("Download content unsupported");
	}

	default Video pollJobResult(String taskId) {
		throw new UnsupportedOperationException("Poll job result allowed only on async models");
	}

}
