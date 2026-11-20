package io.github.mityushin.springai.higgsfield;

import io.github.mityushin.springai.higgsfield.video.VideoOptions;
import java.util.Map;
import java.util.Objects;
import io.github.mityushin.higgsfield.SubmitOptions;
import io.github.mityushin.higgsfield.catalog.ModelDescriptor;
import io.github.mityushin.higgsfield.catalog.ModelType;

/** The model is the Higgsfield endpoint; parameters are sent verbatim to that endpoint. */
public final class HiggsfieldVideoOptions implements VideoOptions {
    private final String model;
    private final Map<String, Object> parameters;
    private final SubmitOptions submitOptions;
    public HiggsfieldVideoOptions(ModelDescriptor model, Map<String, Object> parameters) {
        this(videoId(model), parameters, SubmitOptions.defaults());
    }
    public HiggsfieldVideoOptions(ModelDescriptor model, Map<String, Object> parameters, SubmitOptions submitOptions) {
        this(videoId(model), parameters, submitOptions);
    }
    private static String videoId(ModelDescriptor model) {
        if (Objects.requireNonNull(model).output() != ModelType.VIDEO) throw new IllegalArgumentException("Expected a video model");
        return model.id();
    }
    public HiggsfieldVideoOptions(String model, Map<String, Object> parameters) {
        this(model, parameters, SubmitOptions.defaults());
    }
    public HiggsfieldVideoOptions(String model, Map<String, Object> parameters, SubmitOptions submitOptions) {
        this.model = Objects.requireNonNull(model);
        this.parameters = Map.copyOf(parameters);
        this.submitOptions = Objects.requireNonNull(submitOptions);
    }
    @Override public String getModel() { return model; }
    @Override public String getSourceImageUrl() { Object value = parameters.get("image_url"); return value instanceof String s ? s : null; }
    public Map<String, Object> getParameters() { return parameters; }
    public SubmitOptions getSubmitOptions() { return submitOptions; }
}
