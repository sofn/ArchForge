package com.lesofn.archforge.starter.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.junit.jupiter.api.Test;

/** admin and web export to the same collector: their spans must not both be called "ArchForge". */
class TracingServiceNameTest {

    @Test
    void theTraceResourceIsNamedAfterTheApplication() {
        OpenTelemetry openTelemetry = new TracingAutoConfiguration().openTelemetry("http://localhost:4318/v1/traces", 0.1,
                "server-web");

        String provider = ((OpenTelemetrySdk) openTelemetry).getSdkTracerProvider().toString();
        ((OpenTelemetrySdk) openTelemetry).close();

        assertThat(provider).contains("service.name=\"server-web\"");
    }
}
