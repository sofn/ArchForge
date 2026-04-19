package com.lesofn.archsmith.infrastructure.aspect;

import com.lesofn.archsmith.common.error.system.SystemException;
import com.lesofn.archsmith.common.errors.SystemErrorCode;
import com.lesofn.archsmith.infrastructure.annotation.RateLimit;
import com.lesofn.archsmith.infrastructure.auth.AuthenticationUtils;
import com.lesofn.archsmith.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archsmith.infrastructure.frame.context.RequestContext;
import com.lesofn.archsmith.infrastructure.frame.context.ScopedValueContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

/**
 * 限流切面：基于 Redis + Lua 原子自增计数。
 *
 * @author sofn
 */
@Slf4j
@Aspect
@Component
@Order(0)
@RequiredArgsConstructor
public class RateLimitAspect {

    private static final String LUA_SCRIPT =
            """
            local current = redis.call('INCR', KEYS[1])
            if tonumber(current) == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return current
            """;

    private static final DefaultRedisScript<Long> LIMIT_SCRIPT =
            new DefaultRedisScript<>(LUA_SCRIPT, Long.class);

    private final StringRedisTemplate redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        String redisKey = buildKey(point, rateLimit);
        List<String> keys = Collections.singletonList(redisKey);
        Long count = redisTemplate.execute(LIMIT_SCRIPT, keys, String.valueOf(rateLimit.time()));
        if (count == null || count > rateLimit.maxCount()) {
            log.warn(
                    "RateLimit exceeded: key={}, count={}, max={}",
                    redisKey,
                    count,
                    rateLimit.maxCount());
            throw new SystemException(SystemErrorCode.E_RATE_LIMIT_EXCEEDED);
        }
        return point.proceed();
    }

    private String buildKey(ProceedingJoinPoint point, RateLimit rateLimit) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        String baseKey =
                rateLimit.key().isEmpty()
                        ? signature.getDeclaringTypeName() + "." + signature.getName()
                        : rateLimit.key();
        String dimension =
                switch (rateLimit.limitType()) {
                    case GLOBAL -> "global";
                    case IP -> "ip:" + getClientIp();
                    case USER -> "user:" + getCurrentUserId();
                };
        return "rate:limit:" + baseKey + ":" + dimension;
    }

    private String getClientIp() {
        RequestContext ctx = ScopedValueContext.getRequestContext();
        if (ctx != null && ctx.getOriginRequest() != null) {
            HttpServletRequest request = ctx.getOriginRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isEmpty()) {
                return forwarded.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return "unknown";
    }

    private String getCurrentUserId() {
        try {
            SystemLoginUser user = AuthenticationUtils.getSystemLoginUser();
            return user != null ? String.valueOf(user.getUserId()) : "anonymous";
        } catch (Exception e) {
            return "anonymous";
        }
    }
}
