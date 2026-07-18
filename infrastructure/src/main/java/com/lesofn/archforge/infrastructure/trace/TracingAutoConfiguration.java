package com.lesofn.archforge.infrastructure.trace;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.otel.bridge.OtelBaggageManager;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.List;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Manual OpenTelemetry + Micrometer Tracing bridge configuration for Spring Boot 4.x
 * environments that do not auto-configure an OTLP tracer.
 */
@Configuration
public class TracingAutoConfiguration {

    private static final String DEFAULT_ENDPOINT = "http://localhost:4318/v1/traces";
    private static final String SERVICE_NAME = "ArchForge";

    @Bean
    @ConditionalOnMissingBean
    public OpenTelemetry openTelemetry(
            @Value("${management.tracing.otlp.endpoint:${OTEL_EXPORTER_OTLP_ENDPOINT:" + DEFAULT_ENDPOINT +
                    "}}") String endpoint,
            @Value("${management.tracing.sampling.probability:${SAMPLING_PROBABILITY:1.0}}") double probability) {
        OtlpHttpSpanExporter spanExporter = OtlpHttpSpanExporter.builder().setEndpoint(endpoint).build();
        BatchSpanProcessor spanProcessor = BatchSpanProcessor.builder(spanExporter).build();
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(AttributeKey.stringKey("service.name"), SERVICE_NAME)));
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(spanProcessor)
                .setSampler(Sampler.traceIdRatioBased(probability))
                .setResource(resource)
                .build();
        return OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
    }

    @Bean
    @ConditionalOnMissingBean(Tracer.class)
    public OtelTracer otelTracer(OpenTelemetry openTelemetry) {
        io.opentelemetry.api.trace.Tracer tracer = openTelemetry.getTracer(SERVICE_NAME);
        OtelCurrentTraceContext traceContext = new OtelCurrentTraceContext();
        OtelBaggageManager baggageManager = new OtelBaggageManager(traceContext, List.of(), List.of());
        return new OtelTracer(tracer, traceContext, event -> {
        }, baggageManager);
    }

    @Bean
    public SmartInitializingSingleton tracingObservationHandlerRegistrar(ObservationRegistry observationRegistry,
            OtelTracer otelTracer) {
        return () -> {
            DefaultTracingObservationHandler handler = new DefaultTracingObservationHandler(otelTracer);
            observationRegistry.observationConfig().observationHandler(handler);
        };
    }
}
