package com.lesofn.archforge.infrastructure.aspect;

import com.google.common.hash.Hashing;
import com.lesofn.archforge.infrastructure.annotation.Idempotent;
import com.lesofn.archforge.infrastructure.annotation.IdempotentType;
import com.lesofn.archforge.infrastructure.auth.AuthenticationUtils;
import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.frame.context.RequestContext;
import com.lesofn.archforge.infrastructure.frame.context.ScopedValueContext;
import com.lesofn.archforge.infrastructure.security.SecurityErrorCode;
import com.lesofn.archforge.infrastructure.security.SecurityException;
import com.lesofn.archforge.infrastructure.security.idempotent.IdempotentProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * 幂等 AOP 切面。
 *
 * <p>
 * 支持 PARAM / TOKEN / HEADER 三种模式，基于 Redisson 实现分布式幂等控制。
 *
 * @author sofn
 */
@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "arch-forge.idempotent", name = "enabled", havingValue = "true", matchIfMissing = true)
public class IdempotentAspect {

    private static final String ANONYMOUS = "anonymous";

    private final RedissonClient redissonClient;
    private final IdempotentProperties idempotentProperties;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint point, Idempotent idempotent) throws Throwable {
        IdempotentType type = idempotent.type();
        switch (type) {
            case TOKEN -> handleToken(idempotent);
            case HEADER -> handleHeader(point, idempotent);
            case PARAM -> handleParam(point, idempotent);
            default -> throw new SecurityException(SecurityErrorCode.IDEMPOTENT_REJECT);
        }
        return point.proceed();
    }

    private void handleToken(Idempotent idempotent) {
        String token = resolveTokenHeader(idempotent.header());
        if (token == null || token.isBlank()) {
            throw new SecurityException(SecurityErrorCode.IDEMPOTENT_TOKEN_MISSING);
        }
        String key = idempotentProperties.getTokenPrefix() + token;
        RBucket<String> bucket = redissonClient.getBucket(key);
        String value = bucket.getAndDelete();
        if (value == null) {
            log.warn("Invalid or expired idempotent token: {}", token);
            throw new SecurityException(SecurityErrorCode.IDEMPOTENT_TOKEN_INVALID);
        }
    }

    private void handleHeader(ProceedingJoinPoint point, Idempotent idempotent) {
        String headerValue = resolveTokenHeader(idempotent.header());
        if (headerValue == null || headerValue.isBlank()) {
            throw new SecurityException(SecurityErrorCode.IDEMPOTENT_TOKEN_MISSING);
        }
        String baseKey = buildBaseKey(point, idempotent);
        String key = idempotentProperties.getKeyPrefix() + baseKey + ":" + headerValue;
        if (!tryLock(key, idempotent.expireSeconds())) {
            log.warn("Duplicate header idempotent request: {}", key);
            throw new SecurityException(SecurityErrorCode.IDEMPOTENT_REJECT);
        }
    }

    private void handleParam(ProceedingJoinPoint point, Idempotent idempotent) {
        String key = idempotentProperties.getKeyPrefix() + buildBaseKey(point, idempotent);
        if (!tryLock(key, idempotent.expireSeconds())) {
            log.warn("Duplicate param idempotent request: {}", key);
            throw new SecurityException(SecurityErrorCode.IDEMPOTENT_REJECT);
        }
    }

    private boolean tryLock(String key, long expireSeconds) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.setIfAbsent("1", Duration.ofSeconds(expireSeconds));
    }

    private String buildBaseKey(ProceedingJoinPoint point, Idempotent idempotent) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        String keyExpression = idempotent.key();
        if (keyExpression != null && !keyExpression.isBlank()) {
            Object[] args = point.getArgs();
            String[] paramNames = new DefaultParameterNameDiscoverer().getParameterNames(signature.getMethod());
            StandardEvaluationContext context = new StandardEvaluationContext();
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length && i < args.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }
            try {
                return new SpelExpressionParser().parseExpression(keyExpression).getValue(context, String.class);
            } catch (Exception e) {
                log.warn("Failed to parse idempotent key expression: {}", keyExpression, e);
            }
        }
        String methodKey = signature.getDeclaringTypeName() + "." + signature.getName();
        String userId = resolveUserId();
        String argsHash = hashArgs(point.getArgs());
        return methodKey + ":" + userId + ":" + argsHash;
    }

    private String resolveTokenHeader(String headerName) {
        RequestContext ctx = ScopedValueContext.getRequestContext();
        if (ctx == null || ctx.getOriginRequest() == null) {
            return null;
        }
        HttpServletRequest request = ctx.getOriginRequest();
        return request.getHeader(headerName != null && !headerName.isBlank() ? headerName
                : idempotentProperties.getHeaderName());
    }

    private String resolveUserId() {
        try {
            SystemLoginUser user = AuthenticationUtils.getSystemLoginUser();
            if (user != null) {
                return String.valueOf(user.getUserId());
            }
        } catch (Exception ignored) {
            // Fall through to request-based identifier
        }
        RequestContext ctx = ScopedValueContext.getRequestContext();
        if (ctx != null && ctx.getOriginRequest() != null) {
            return "ip:" + ctx.getOriginRequest().getRemoteAddr();
        }
        return ANONYMOUS;
    }

    @SuppressWarnings("deprecation")
    private String hashArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "noargs";
        }
        String joined = Arrays.deepToString(args);
        return Hashing.sha256().hashString(joined, StandardCharsets.UTF_8).toString();
    }
}
