package io.github.mityushin.higgsfield;

import io.github.mityushin.higgsfield.catalog.*;
import io.github.mityushin.springai.higgsfield.*;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class SpringModelCatalogTest {

	@Test
	void modelOptionsAcceptMatchingCatalogDescriptors() {
		var models = ModelCatalog.bundled();
		var video = models.get(HiggsfieldModels.SEEDANCE_2_0_TEXT_TO_VIDEO);
		var image = models.get(HiggsfieldModels.MARKETING_STUDIO_IMAGE);
		assertEquals(video.id(), new HiggsfieldVideoOptions(video, Map.of()).getModel());
		assertEquals(image.id(), new HiggsfieldImageOptions(image, Map.of()).getModel());
		assertThrows(IllegalArgumentException.class, () -> new HiggsfieldVideoOptions(image, Map.of()));
		assertThrows(IllegalArgumentException.class, () -> new HiggsfieldImageOptions(video, Map.of()));
	}

}
