package com.lesofn.archforge.infrastructure.frame.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.lesofn.archforge.infrastructure.annotation.RepeatSubmit;
import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import com.lesofn.archforge.infrastructure.db.redis.RedisUtil;
import com.lesofn.archforge.infrastructure.frame.filter.RepeatableRequestWrapper;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

/**
 * The body-caching wrapper is not the outermost request any more (the request-log filter wraps it again), so an
 * {@code instanceof} check missed it: every JSON submission was fingerprinted as the empty parameter map, i.e. any two
 * JSON posts to the same URL looked like duplicates. Same root cause as the API-signature fix.
 */
class RepeatSubmitInterceptorTest {

    private final RedisUtil redisUtil = mock(RedisUtil.class);
    private final RepeatSubmitInterceptor interceptor = new RepeatSubmitInterceptor(redisUtil, new ArchForgeProperties());

    @RepeatSubmit
    @SuppressWarnings("unused")
    public void submit() {
    }

    @Test
    @SuppressWarnings("unchecked")
    void fingerprintsTheJsonBodyThroughStackedWrappers() throws Exception {
        MockHttpServletRequest raw = new MockHttpServletRequest("POST", "/admin/demo");
        raw.setContentType("application/json");
        raw.setContent("{\"title\":\"a\"}".getBytes(StandardCharsets.UTF_8));
        HttpServletRequestWrapper outer = new HttpServletRequestWrapper(new RepeatableRequestWrapper(raw));
        HandlerMethod handler = new HandlerMethod(this, RepeatSubmitInterceptorTest.class.getMethod("submit"));

        assertTrue(interceptor.preHandle(outer, new MockHttpServletResponse(), handler));

        ArgumentCaptor<Map<String, Object>> cached = ArgumentCaptor.forClass(Map.class);
        verify(redisUtil).setCacheObject(anyString(), cached.capture(), anyInt(), eq(TimeUnit.MILLISECONDS));
        Map<String, Object> fingerprint = (Map<String, Object>) Objects.requireNonNull(cached.getValue().get("/admin/demo"));
        assertEquals("{\"title\":\"a\"}", fingerprint.get("repeatParams"));
    }
}
