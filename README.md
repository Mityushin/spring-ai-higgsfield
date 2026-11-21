# Spring AI Higgsfield

An unofficial Java integration of Higgsfield image and video generation with Spring AI. Requires Java 25 and Spring AI 2.0.1. This project is not affiliated with Higgsfield or Spring.

The SDK dependency is `io.github.mityushin:higgsfield-java-sdk`. Both libraries are currently snapshots; published artifacts are not yet available.

`HiggsfieldImageModel` implements Spring AI `ImageModel`. `HiggsfieldAsyncImageModel` exposes submit and poll operations through this library's own asynchronous image interface. `HiggsfieldVideoModel` provides submit and poll operations through this library's video interfaces; these are not official Spring AI contracts.

```java
import io.github.mityushin.higgsfield.HiggsfieldClient;
import io.github.mityushin.higgsfield.catalog.HiggsfieldModels;
import io.github.mityushin.springai.higgsfield.HiggsfieldVideoModel;
import io.github.mityushin.springai.higgsfield.HiggsfieldVideoOptions;
import io.github.mityushin.springai.higgsfield.video.VideoPrompt;
import java.util.Map;

try (var client = HiggsfieldClient.fromEnvironment()) {
    var model = new HiggsfieldVideoModel(client);
    var descriptor = client.models().get(HiggsfieldModels.SEEDANCE_2_0_TEXT_TO_VIDEO);
    var response = model.call(new VideoPrompt("A cinematic sunset",
        new HiggsfieldVideoOptions(descriptor, Map.of("duration", 5))));
    String requestId = response.getResult().getOutput().getTaskId();
    System.out.println(requestId);
}
```

Use the request ID to poll the job after submission. Inputs depend on the chosen endpoint; model-specific parameters can be passed directly through the options map. The bundled catalog is an offline, incomplete snapshot and does not establish account access. Output URLs are temporary. API calls can consume credits.

Licensed under the MIT License; see [LICENSE](LICENSE).

## Formatting

Java source and tests follow Spring Java Format. `mvn verify` checks formatting as part of the build. To apply the Java formatter and check the POM, run:

```bash
mvn io.spring.javaformat:spring-javaformat-maven-plugin:0.0.46:apply
python3 tools/format_pom.py --write
python3 tools/format_pom.py --check
```

CI checks the POM format separately; the POM formatter requires Python 3.
