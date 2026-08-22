package com.lesofn.archforge.starter.trace;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.opentelemetry.api.OpenTelemetry;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestApplication.class)
@Tag("slow")
class TracingAutoConfigurationTest {

    @Autowired
    private OpenTelemetry openTelemetry;

    @Autowired
    private Tracer tracer;

    @Autowired
    private ObservationRegistry observationRegistry;

    @Test
    void shouldCreateOpenTelemetryBean() {
        Assertions.assertThat(openTelemetry).isNotNull();
    }

    @Test
    void shouldCreateTracerBean() {
        Assertions.assertThat(tracer).isNotNull();
    }

    @Test
    void shouldCreateObservationRegistryBean() {
        Assertions.assertThat(observationRegistry).isNotNull();
    }

    @Test
    void shouldBindObservationToTracer() {
        Assertions.assertThat(observationRegistry).isNotNull();
        Observation observation = Observation.createNotStarted("test.observation", observationRegistry);
        observation.start();
        observation.stop();
    }
}
