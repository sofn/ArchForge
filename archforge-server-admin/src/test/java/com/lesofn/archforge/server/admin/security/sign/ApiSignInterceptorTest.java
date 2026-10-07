package com.lesofn.archforge.server.admin.security.sign;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.infrastructure.security.SecurityException;
import com.lesofn.archforge.infrastructure.security.sign.ApiSign;
import com.lesofn.archforge.infrastructure.frame.filter.RepeatableRequestWrapper;
import com.lesofn.archforge.infrastructure.security.sign.ApiSignInterceptor;
import com.lesofn.archforge.starter.requestlog.RequestLogRequestWrapper;
import com.lesofn.archforge.infrastructure.security.sign.ApiSignProperties;
import com.lesofn.archforge.infrastructure.security.sign.AppKeyProvider;
import com.lesofn.archforge.infrastructure.security.sign.ConfigAppKeyProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.method.HandlerMethod;

/**
 * {@link ApiSignInterceptor} 单元测试。
 *
 * @author sofn
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApiSignInterceptorTest {

    private static final String APP_KEY = "test-app";
    private static final String APP_SECRET = "test-secret";

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RBucket<String> nonceBucket;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private ApiSignInterceptor interceptor;

    @BeforeEach
    void setUp() {
        ApiSignProperties properties = new ApiSignProperties();
        properties.getApps().put(APP_KEY, APP_SECRET);
        AppKeyProvider provider = new ConfigAppKeyProvider(properties);
        when(redissonClient.<String> getBucket(anyString())).thenReturn(nonceBucket);
        interceptor = new ApiSignInterceptor(redissonClient, provider, properties);
    }

    @Test
    void validSignature_passes() throws Exception {
        long timestamp = System.currentTimeMillis();
        String nonce = "abc123";
        String sign = sign(timestamp, nonce, "");
        HandlerMethod handler = handlerMethod("signed");

        mockRequestHeaders(timestamp, nonce, sign);
        when(nonceBucket.setIfAbsent(anyString(), any(Duration.class))).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, handler);

        assertTrue(result);
    }

    @Test
    void missingHeaders_throwsInvalid() throws Exception {
        when(request.getHeader("X-App-Key")).thenReturn(null);
        when(request.getHeader("X-Timestamp")).thenReturn(null);
        when(request.getHeader("X-Nonce")).thenReturn(null);
        when(request.getHeader("X-Sign")).thenReturn(null);

        HandlerMethod handler = handlerMethod("signed");

        assertThrows(SecurityException.class, () -> interceptor.preHandle(request, response, handler));
    }

    @Test
    void expiredTimestamp_throwsExpired() throws Exception {
        long timestamp = System.currentTimeMillis() - 600_000;
        String nonce = "nonce1";
        String sign = sign(timestamp, nonce, "");
        HandlerMethod handler = handlerMethod("signed");

        mockRequestHeaders(timestamp, nonce, sign);

        assertThrows(SecurityException.class, () -> interceptor.preHandle(request, response, handler));
    }

    @Test
    void invalidSignature_throwsInvalid() throws Exception {
        long timestamp = System.currentTimeMillis();
        String nonce = "nonce2";
        HandlerMethod handler = handlerMethod("signed");

        mockRequestHeaders(timestamp, nonce, "wrong-sign");

        assertThrows(SecurityException.class, () -> interceptor.preHandle(request, response, handler));
    }

    @Test
    void replayNonce_throwsReplay() throws Exception {
        long timestamp = System.currentTimeMillis();
        String nonce = "nonce3";
        String sign = sign(timestamp, nonce, "");
        HandlerMethod handler = handlerMethod("signed");

        mockRequestHeaders(timestamp, nonce, sign);
        when(nonceBucket.setIfAbsent(anyString(), any(Duration.class))).thenReturn(false);

        assertThrows(SecurityException.class, () -> interceptor.preHandle(request, response, handler));
    }

    @Test
    void unknownAppKey_throwsNotFound() throws Exception {
        long timestamp = System.currentTimeMillis();
        String nonce = "nonce4";
        String sign = sign(timestamp, nonce, "");
        HandlerMethod handler = handlerMethod("signed");

        when(request.getHeader("X-App-Key")).thenReturn("unknown");
        when(request.getHeader("X-Timestamp")).thenReturn(String.valueOf(timestamp));
        when(request.getHeader("X-Nonce")).thenReturn(nonce);
        when(request.getHeader("X-Sign")).thenReturn(sign);

        assertThrows(SecurityException.class, () -> interceptor.preHandle(request, response, handler));
    }

    /**
     * Production wraps twice: RepeatableFilter, then the request-log filter's own caching wrapper on top. The body must
     * still be found (and signed) through the chain.
     */
    @Test
    void signedBodyIsVerifiedThroughStackedWrappers() throws Exception {
        long timestamp = System.currentTimeMillis();
        String body = "{\"amount\":1}";
        HttpServletRequest chain = wrappedJsonRequest(body, timestamp, "nonce5", sign(timestamp, "nonce5", body));
        when(nonceBucket.setIfAbsent(anyString(), any(Duration.class))).thenReturn(true);

        assertTrue(interceptor.preHandle(chain, response, handlerMethod("signed")));
    }

    /** SEC-L6: a signature over an empty body must not authorize a request that carries one. */
    @Test
    void signatureOverEmptyBodyDoesNotCoverARealBody() throws Exception {
        long timestamp = System.currentTimeMillis();
        HttpServletRequest chain = wrappedJsonRequest("{\"amount\":1000}", timestamp, "nonce6",
                sign(timestamp, "nonce6", ""));
        when(nonceBucket.setIfAbsent(anyString(), any(Duration.class))).thenReturn(true);

        assertThrows(SecurityException.class, () -> interceptor.preHandle(chain, response, handlerMethod("signed")));
    }

    /** A body that cannot be re-read (no caching wrapper anywhere) fails closed instead of being signed as "". */
    @Test
    void unreadableBodyFailsClosed() throws Exception {
        long timestamp = System.currentTimeMillis();
        MockHttpServletRequest raw = new MockHttpServletRequest("POST", "/signed");
        raw.setContentType("application/x-www-form-urlencoded");
        raw.setContent("amount=1000".getBytes(StandardCharsets.UTF_8));
        signHeaders(raw, timestamp, "nonce7", sign(timestamp, "nonce7", ""));
        when(nonceBucket.setIfAbsent(anyString(), any(Duration.class))).thenReturn(true);

        assertThrows(SecurityException.class, () -> interceptor.preHandle(raw, response, handlerMethod("signed")));
    }

    private static HttpServletRequest wrappedJsonRequest(String body, long timestamp, String nonce, String sign)
            throws Exception {
        MockHttpServletRequest raw = new MockHttpServletRequest("POST", "/signed");
        raw.setContentType("application/json");
        raw.setContent(body.getBytes(StandardCharsets.UTF_8));
        signHeaders(raw, timestamp, nonce, sign);
        return new RequestLogRequestWrapper(new RepeatableRequestWrapper(raw));
    }

    private static void signHeaders(MockHttpServletRequest raw, long timestamp, String nonce, String sign) {
        raw.addHeader("X-App-Key", APP_KEY);
        raw.addHeader("X-Timestamp", String.valueOf(timestamp));
        raw.addHeader("X-Nonce", nonce);
        raw.addHeader("X-Sign", sign);
    }

    private void mockRequestHeaders(long timestamp, String nonce, String sign) {
        when(request.getHeader("X-App-Key")).thenReturn(APP_KEY);
        when(request.getHeader("X-Timestamp")).thenReturn(String.valueOf(timestamp));
        when(request.getHeader("X-Nonce")).thenReturn(nonce);
        when(request.getHeader("X-Sign")).thenReturn(sign);
    }

    private String sign(long timestamp, String nonce, String body) throws Exception {
        String payload = APP_KEY + timestamp + nonce + body;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(APP_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] bytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(bytes).toLowerCase(Locale.ROOT);
    }

    private HandlerMethod handlerMethod(String name) throws NoSuchMethodException {
        Method method = TestController.class.getMethod(name);
        return new HandlerMethod(new TestController(), method);
    }

    @SuppressWarnings({
            "UnusedMethod", "EffectivelyPrivate"
    }) // 方法经 TestController.class.getMethod(name) 反射取用
    private static class TestController {
        @ApiSign
        public void signed() {
        }
    }
}
