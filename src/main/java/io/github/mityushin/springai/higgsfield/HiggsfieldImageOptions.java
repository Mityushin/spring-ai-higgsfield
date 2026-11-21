package io.github.mityushin.springai.higgsfield;

import org.springframework.ai.image.ImageOptions;
import java.util.Map;
import java.util.Objects;
import io.github.mityushin.higgsfield.SubmitOptions;
import io.github.mityushin.higgsfield.catalog.ModelDescriptor;
import io.github.mityushin.higgsfield.catalog.ModelType;

/** Model-specific input fields are passed through without assuming a common schema. */
public final class HiggsfieldImageOptions implements ImageOptions {

	private final String endpoint;

	private final Map<String, Object> parameters;

	private final SubmitOptions submitOptions;

	public HiggsfieldImageOptions(ModelDescriptor model, Map<String, Object> parameters) {
		this(imageId(model), parameters, SubmitOptions.defaults());
	}

	public HiggsfieldImageOptions(ModelDescriptor model, Map<String, Object> parameters, SubmitOptions submitOptions) {
		this(imageId(model), parameters, submitOptions);
	}

	private static String imageId(ModelDescriptor model) {
		if (Objects.requireNonNull(model).output() != ModelType.IMAGE)
			throw new IllegalArgumentException("Expected an image model");
		return model.id();
	}

	public HiggsfieldImageOptions(String endpoint, Map<String, Object> parameters) {
		this(endpoint, parameters, SubmitOptions.defaults());
	}

	public HiggsfieldImageOptions(String endpoint, Map<String, Object> parameters, SubmitOptions submitOptions) {
		this.endpoint = Objects.requireNonNull(endpoint);
		this.parameters = Map.copyOf(parameters);
		this.submitOptions = Objects.requireNonNull(submitOptions);
	}

	@Override
	public String getModel() {
		return endpoint;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public SubmitOptions getSubmitOptions() {
		return submitOptions;
	}

	@Override
	public Integer getN() {
		return null;
	}

	@Override
	public Integer getWidth() {
		return null;
	}

	@Override
	public Integer getHeight() {
		return null;
	}

	@Override
	public String getResponseFormat() {
		return null;
	}

	@Override
	public String getStyle() {
		return null;
	}

}
