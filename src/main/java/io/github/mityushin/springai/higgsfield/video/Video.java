package io.github.mityushin.springai.higgsfield.video;

import lombok.Getter;

@Getter
public class Video {

	private final String url;

	private final String taskId;

	private final VideoJobStatus status;

	private final String errorCode;

	private final String ratio;

	private final byte[] content;

	private Video(String url, String taskId, VideoJobStatus status) {
		this(url, taskId, status, null, null, null);
	}

	private Video(String url, String taskId, VideoJobStatus status, String errorCode, String ratio, byte[] content) {
		this.url = url;
		this.taskId = taskId;
		this.status = status;
		this.errorCode = errorCode;
		this.ratio = ratio;
		this.content = content;
	}

	public static Video ofUrl(String url) {
		return new Video(url, null, VideoJobStatus.READY);
	}

	/**
	 * Creates a ready video with its CDN URL and the resolved output aspect ratio (e.g.
	 * {@code 9:16}), as reported by providers that adapt the ratio from input assets
	 * (first-frame/first-last-frame generation).
	 */
	public static Video ofUrl(String url, String ratio) {
		return new Video(url, null, VideoJobStatus.READY, null, ratio, null);
	}

	/** Creates a ready video whose content was returned inline by the provider. */
	public static Video ofContent(byte[] content, String ratio) {
		return new Video(null, null, VideoJobStatus.READY, null, ratio, content);
	}

	public static Video ofTaskId(String taskId) {
		return new Video(null, taskId, VideoJobStatus.IN_PROGRESS);
	}

	public static Video ofReadyTaskId(String taskId) {
		return new Video(null, taskId, VideoJobStatus.READY);
	}

	public static Video ofFailedTaskId(String taskId) {
		return new Video(null, taskId, VideoJobStatus.ERROR, "Video generation failed. Please try again later.", null,
				null);
	}

	public static Video ofFailedTaskId(String taskId, String errorCode) {
		return new Video(null, taskId, VideoJobStatus.ERROR, errorCode, null, null);
	}

	@Override
	public String toString() {
		return "Video{url='%s', taskId='%s'}".formatted(url, taskId);
	}

}
