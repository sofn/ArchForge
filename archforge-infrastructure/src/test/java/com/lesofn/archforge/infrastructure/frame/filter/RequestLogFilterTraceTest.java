package com.lesofn.archforge.infrastructure.frame.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.otel.bridge.OtelBaggageManager;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * The request filter is the outermost one: it has to make its span current for the whole request and expose the
 * trace id through MDC — otherwise logs cannot be correlated with traces — and it must not emit a second
 * {@code http.server.requests} metric family next to Spring Boot's.
 */
class RequestLogFilterTraceTest {

    private OtelTracer tracer;
    private ObservationRegistry registry;
    private final List<Observation.Context> started = new ArrayList<>();

    @BeforeEach
    void setUp() {
        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder().setTracerProvider(SdkTracerProvider.builder().build()).build();
        OtelCurrentTraceContext traceContext = new OtelCurrentTraceContext();
        tracer = new OtelTracer(sdk.getTracer("test"), traceContext, event -> {
        }, new OtelBaggageManager(traceContext, List.of(), List.of()));
        registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(new DefaultTracingObservationHandler(tracer));
        registry.observationConfig().observationHandler(new ObservationHandler<Observation.Context>() {
            @Override
            public boolean supportsContext(Observation.Context context) {
                return true;
            }

            @Override
            public void onStart(Observation.Context context) {
                started.add(context);
            }
        });
    }

    @Test
    void traceIdIsInMdcForTheWholeRequestAndGoneAfterwards() throws Exception {
        RequestLogFilter filter = new RequestLogFilter(registry, tracer);
        String[] inside = new String[3];

        filter.doFilter(new MockHttpServletRequest("GET", "/admin/user/42"), new MockHttpServletResponse(),
                (req, res) -> {
                    inside[0] = MDC.get("traceId");
                    inside[1] = MDC.get("spanId");
                    Span current = tracer.currentSpan();
                    inside[2] = current == null ? null : current.context().traceId();
                });

        assertNotNull(inside[0], "traceId missing from MDC");
        assertEquals(32, inside[0].length());
        assertEquals(inside[2], inside[0], "MDC must carry the id of the span that is current for the request");
        assertNotNull(inside[1]);
        assertNull(MDC.get("traceId"));
        assertNull(MDC.get("spanId"));
        assertNull(MDC.get("requestId"));
    }

    @Test
    void requestWithoutATracerStillWorks() throws Exception {
        RequestLogFilter filter = new RequestLogFilter(registry, null);
        String[] inside = new String[1];

        filter.doFilter(new MockHttpServletRequest("GET", "/admin/user/42"), new MockHttpServletResponse(),
                (req, res) -> inside[0] = MDC.get("requestId"));

        assertNotNull(inside[0]);
        assertNull(MDC.get("traceId"));
    }

    @Test
    void doesNotShadowSpringBootsHttpServerRequestsMetric() throws Exception {
        RequestLogFilter filter = new RequestLogFilter(registry, tracer);

        filter.doFilter(new MockHttpServletRequest("GET", "/admin/user/42"), new MockHttpServletResponse(),
                (req, res) -> {
                });

        assertEquals(1, started.size());
        Observation.Context context = started.get(0);
        // two observations named http.server.requests double every request in sum() and alert ratios
        assertFalse("http.server.requests".equals(context.getName()), context.getName());
        // the raw path (ids included) is a trace attribute, never a metric label
        assertTrue(context.getHighCardinalityKeyValues().stream().anyMatch(kv -> kv.getKey().equals("http.path")));
        assertFalse(context.getLowCardinalityKeyValues().stream().anyMatch(kv -> kv.getKey().equals("http.path")));
    }
}
