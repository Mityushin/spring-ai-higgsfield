package io.github.mityushin.springai.higgsfield.video;

import org.springframework.ai.model.ModelResponse;
import org.springframework.ai.model.MutableResponseMetadata;
import org.springframework.ai.model.ResponseMetadata;

import java.util.List;

public class VideoResponse implements ModelResponse<VideoGeneration> {

    private final List<VideoGeneration> generations;

    public VideoResponse(VideoGeneration generation) {
        this(List.of(generation));
    }

    public VideoResponse(List<VideoGeneration> generations) {
        this.generations = List.copyOf(generations);
    }

    @Override
    public VideoGeneration getResult() {
        return generations.isEmpty() ? null : generations.get(0);
    }

    @Override
    public List<VideoGeneration> getResults() {
        return generations;
    }

    @Override
    public ResponseMetadata getMetadata() {
        return new MutableResponseMetadata();
    }
}
