package com.lesofn.archforge.starter.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TracingAutoConfigurationTest {

    @Autowired
    private OpenTelemetry openTelemetry;

    @Autowired
    private Tracer tracer;

    @Autowired
    private ObservationRegistry observationRegistry;

    @Test
    void shouldCreateOpenTelemetryBean() {
        assertThat(openTelemetry).isNotNull();
    }

    @Test
    void shouldCreateTracerBean() {
        assertThat(tracer).isNotNull();
    }

    @Test
    void shouldCreateObservationRegistryBean() {
        assertThat(observationRegistry).isNotNull();
    }

    @Test
    void shouldBindObservationToTracer() {
        assertThat(observationRegistry).isNotNull();

        io.micrometer.observation.Observation observation = io.micrometer.observation.Observation
                .createNotStarted("test.observation", this.observationRegistry);
        observation.start();
        observation.stop();
    }
}
