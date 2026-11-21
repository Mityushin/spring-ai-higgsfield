package io.github.mityushin.springai.higgsfield.video;

import org.springframework.ai.model.ModelResult;
import org.springframework.ai.model.ResultMetadata;

public class VideoGeneration implements ModelResult<Video> {

	private final Video video;

	public VideoGeneration(Video video) {
		this.video = video;
	}

	@Override
	public Video getOutput() {
		return video;
	}

	@Override
	public ResultMetadata getMetadata() {
		return new ResultMetadata() {
		};
	}

}
