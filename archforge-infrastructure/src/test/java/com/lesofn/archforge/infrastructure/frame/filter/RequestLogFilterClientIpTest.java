package com.lesofn.archforge.infrastructure.frame.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lesofn.archforge.infrastructure.frame.context.ScopedValueContext;
import io.micrometer.observation.ObservationRegistry;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** The request filter is the one place the client IP is resolved; everything downstream reads it from the context. */
class RequestLogFilterClientIpTest {

    @Test
    void aForgedForwardedForFromAnUntrustedPeerIsIgnored() throws Exception {
        assertEquals("203.0.113.7", resolvedIp(List.of(), "203.0.113.7", "6.6.6.6"));
    }

    @Test
    void behindATrustedProxyTheClientIsTheAddressTheProxyAppended() throws Exception {
        assertEquals("198.51.100.9", resolvedIp(List.of("10.0.0.2"), "10.0.0.2", "6.6.6.6, 198.51.100.9"));
    }

    private static String resolvedIp(List<String> trustedProxies, String peer, String forwardedFor) throws Exception {
        RequestLogFilter filter = new RequestLogFilter(ObservationRegistry.NOOP, null, trustedProxies);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/anything");
        request.setRemoteAddr(peer);
        request.addHeader("X-Forwarded-For", forwardedFor);
        AtomicReference<String> seen = new AtomicReference<>();

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> seen.set(ScopedValueContext.getClientIp()));

        return java.util.Objects.requireNonNull(seen.get());
    }
}
