package com.lesofn.archforge.infrastructure.security.sign;

import com.lesofn.archforge.infrastructure.frame.filter.RepeatableRequestWrapper;
import com.lesofn.archforge.infrastructure.security.SecurityErrorCode;
import com.lesofn.archforge.infrastructure.security.SecurityException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;

/**
 * API 签名拦截器。
 *
 * <p>
 * 校验 {@link ApiSign} 标注接口的请求签名，防止请求篡改与重放攻击。
 *
 * @author sofn
 */
@Slf4j
@RequiredArgsConstructor
public class ApiSignInterceptor implements HandlerInterceptor {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String NONCE_KEY_PREFIX = "apisign:nonce:";

    private final RedissonClient redissonClient;
    private final AppKeyProvider appKeyProvider;
    private final ApiSignProperties apiSignProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        ApiSign apiSign = handlerMethod.getMethodAnnotation(ApiSign.class);
        if (apiSign == null) {
            return true;
        }

        String appKey = request.getHeader(apiSign.appKeyHeader());
        String timestampStr = request.getHeader(apiSign.timestampHeader());
        String nonce = request.getHeader(apiSign.nonceHeader());
        String sign = request.getHeader(apiSign.signHeader());

        if (appKey == null || appKey.isBlank() || timestampStr == null || timestampStr.isBlank() || nonce == null || nonce
                .isBlank() || sign == null || sign.isBlank()) {
            log.warn("Missing api sign headers for: {}", request.getRequestURI());
            throw new SecurityException(SecurityErrorCode.SIGN_INVALID);
        }

        String secret = appKeyProvider.getSecret(appKey);
        if (secret == null) {
            log.warn("App key not found: {}", appKey);
            throw new SecurityException(SecurityErrorCode.APP_KEY_NOT_FOUND);
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            throw new SecurityException(SecurityErrorCode.SIGN_INVALID);
        }

        long now = System.currentTimeMillis();
        long allowedOffset = (long) apiSign.timeoutSeconds() * 1000;
        if (Math.abs(now - timestamp) > allowedOffset) {
            log.warn("API signature expired, timestamp: {}, now: {}", timestamp, now);
            throw new SecurityException(SecurityErrorCode.SIGN_EXPIRED);
        }

        String nonceKey = NONCE_KEY_PREFIX + nonce;
        RBucket<String> nonceBucket = redissonClient.getBucket(nonceKey);
        if (!nonceBucket.setIfAbsent("1", Duration.ofSeconds(apiSignProperties.getNonceTtlSeconds()))) {
            log.warn("Replay attack detected, nonce: {}", nonce);
            throw new SecurityException(SecurityErrorCode.REPLAY_ATTACK);
        }

        String body = resolveBody(request);
        String payload = appKey + timestamp + nonce + body;
        String expected = hmacSha256Hex(secret, payload);

        if (!constantTimeEquals(sign, expected)) {
            log.warn("API signature mismatch for appKey: {}", appKey);
            throw new SecurityException(SecurityErrorCode.SIGN_INVALID);
        }

        return true;
    }

    private String resolveBody(HttpServletRequest request) throws Exception {
        // 过滤器会层层包装（请求日志过滤器又包在 RepeatableRequestWrapper 外面）：顺着包装链找缓存了 body 的那层，
        // 只看最外层会把 body 当成空串验签——合法客户端被拒，签空串的请求却能带任意 body
        RepeatableRequestWrapper repeatable = WebUtils.getNativeRequest(request, RepeatableRequestWrapper.class);
        if (repeatable != null) {
            return new String(repeatable.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
        if (request.getContentLengthLong() > 0 || request.getHeader(HttpHeaders.TRANSFER_ENCODING) != null) {
            // 有 body 却拿不到原文：失败关闭，绝不按空串验签
            log.warn("Signed request body cannot be re-read for: {}", request.getRequestURI());
            throw new SecurityException(SecurityErrorCode.SIGN_INVALID);
        }
        return "";
    }

    private String hmacSha256Hex(String secret, String payload) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        mac.init(keySpec);
        byte[] bytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(bytes).toLowerCase(Locale.ROOT);
    }

    private boolean constantTimeEquals(String a, String b) {
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        return java.security.MessageDigest.isEqual(aBytes, bBytes);
    }
}
