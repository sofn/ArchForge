package com.lesofn.archforge.starter.requestlog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** Unit tests for {@link RequestLogFilter}: capture, masking, exclusion and enricher wiring. */
class RequestLogFilterTest {

    private RequestLogProperties properties;
    private List<RequestLogRecord> captured;
    private RequestLogFilter filter;

    @BeforeEach
    void setUp() {
        properties = new RequestLogProperties();
        captured = new ArrayList<>();
        @SuppressWarnings("unchecked")
        ObjectProvider<RequestLogEnricher> enrichers = mock(ObjectProvider.class);
        when(enrichers.orderedStream()).thenReturn(Stream.empty());
        filter = new RequestLogFilter(properties, enrichers, captured::add);
    }

    @Test
    void logsParametersAndJsonResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/demo/list");
        request.setParameter("name", "tom");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            res.setContentType("application/json");
            res.getWriter().write("{\"code\":0}");
        };

        filter.doFilter(request, response, chain);

        assertEquals(1, captured.size());
        RequestLogRecord record = captured.get(0);
        assertEquals("/demo/list", record.getApi());
        assertEquals("GET", record.getMethod());
        assertEquals(200, record.getResponseStatus());
        assertEquals("{\"code\":0}", record.getResponse());
        assertEquals("tom", java.util.Objects.requireNonNull(record.getParameters().get("name"))[0]);
    }

    @Test
    void masksSensitiveParameterAndJsonBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setContentType("application/json");
        request.setContent("{\"username\":\"u\",\"password\":\"p@ss\"}".getBytes(StandardCharsets.UTF_8));
        request.setParameter("token", "abc123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            res.setContentType("application/json");
            res.getWriter().write("{\"token\":\"t0ken\"}");
        };

        filter.doFilter(request, response, chain);

        RequestLogRecord record = captured.get(0);
        assertEquals("***", java.util.Objects.requireNonNull(record.getParameters().get("token"))[0]);
        assertTrue(java.util.Objects.requireNonNull(record.getPayload()).contains("\"password\":\"***\""));
        assertTrue(java.util.Objects.requireNonNull(record.getResponse()).contains("\"token\":\"***\""));
    }

    @Test
    void requestBodyRemainsReadableDownstream() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/echo");
        request.setContentType("application/json");
        request.setContent("{\"a\":1}".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();
        String[] downstreamBody = new String[1];
        FilterChain chain = (req, res) -> downstreamBody[0] = new String(req.getInputStream()
                .readAllBytes(), StandardCharsets.UTF_8);

        filter.doFilter(request, response, chain);

        assertEquals("{\"a\":1}", downstreamBody[0]);
        assertEquals("{\"a\":1}", captured.get(0).getPayload());
    }

    @Test
    void excludedPathBypassesLogging() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v3/api-docs/all");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
        };

        filter.doFilter(request, response, chain);

        assertTrue(captured.isEmpty());
    }

    @Test
    void nonJsonResponseSkipsBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/page");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            res.setContentType("text/html");
            res.getWriter().write("<html/>");
        };

        filter.doFilter(request, response, chain);

        RequestLogRecord record = captured.get(0);
        assertFalse(record.isWriteBody());
        assertEquals("", record.getResponse());
    }

    @Test
    void errorDispatchUsesOriginalUri() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/error");
        request.setAttribute("jakarta.servlet.error.request_uri", "/real/path");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        filter.doFilter(request, response, chain);

        assertEquals("/real/path", captured.get(0).getApi());
        assertEquals(500, captured.get(0).getResponseStatus());
    }

    @Test
    void enricherBackfillsContextFields() throws Exception {
        @SuppressWarnings("unchecked")
        ObjectProvider<RequestLogEnricher> enrichers = mock(ObjectProvider.class);
        when(enrichers.orderedStream()).thenReturn(Stream.of((record, req) -> record.setUid(42L)));
        RequestLogFilter enriched = new RequestLogFilter(properties, enrichers, captured::add);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/demo");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
        };

        enriched.doFilter(request, response, chain);

        assertEquals(42L, captured.get(0).getUid());
    }

    @Test
    void payloadSanitizesTabsAndNewlines() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/multi");
        request.setContentType("application/json");
        request.setContent("{\n\t\"a\":\t1\n}".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
        };

        filter.doFilter(request, response, chain);

        String payload = captured.get(0).getPayload();
        assertFalse(java.util.Objects.requireNonNull(payload).contains("\t"));
        assertFalse(payload.contains("\n"));
        assertTrue(payload.contains("\"a\": 1"));
    }

    @Test
    void noRequestBodyLeavesPayloadNull() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/empty");
        request.setContentType("application/json");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
        };

        filter.doFilter(request, response, chain);

        assertNull(captured.get(0).getPayload());
    }
}
