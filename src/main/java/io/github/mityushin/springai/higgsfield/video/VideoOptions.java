package io.github.mityushin.springai.higgsfield.video;

import org.springframework.ai.model.ModelOptions;

public interface VideoOptions extends ModelOptions {

    String getModel();

    /** Source image URL for image-to-video generation; null for text-to-video. */
    String getSourceImageUrl();
}
