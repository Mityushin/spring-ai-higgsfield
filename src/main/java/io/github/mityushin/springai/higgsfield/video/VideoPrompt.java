package io.github.mityushin.springai.higgsfield.video;

import org.springframework.ai.model.ModelRequest;

public class VideoPrompt implements ModelRequest<String> {

	private final String text;

	private final VideoOptions options;

	public VideoPrompt(String text, VideoOptions options) {
		this.text = text;
		this.options = options;
	}

	@Override
	public String getInstructions() {
		return text;
	}

	@Override
	public VideoOptions getOptions() {
		return options;
	}

}
