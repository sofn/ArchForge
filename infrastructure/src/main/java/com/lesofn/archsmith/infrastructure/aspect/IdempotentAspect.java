package com.lesofn.archsmith.infrastructure.aspect;

import com.google.common.hash.Hashing;
import com.lesofn.archsmith.common.error.system.SystemException;
import com.lesofn.archsmith.common.errors.SystemErrorCode;
import com.lesofn.archsmith.infrastructure.annotation.Idempotent;
import com.lesofn.archsmith.infrastructure.auth.AuthenticationUtils;
import com.lesofn.archsmith.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archsmith.infrastructure.frame.context.RequestContext;
import com.lesofn.archsmith.infrastructure.frame.context.ScopedValueContext;
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
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 防重复提交切面：基于 Redis SETNX + TTL。
 *
 * @author sofn
 */
@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class IdempotentAspect {

    private final StringRedisTemplate redisTemplate;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint point, Idempotent idempotent) throws Throwable {
        String redisKey = buildKey(point, idempotent);
        Boolean acquired =
                redisTemplate
                        .opsForValue()
                        .setIfAbsent(redisKey, "1", Duration.ofSeconds(idempotent.interval()));
        if (Boolean.FALSE.equals(acquired)) {
            log.warn("Duplicate request detected: key={}", redisKey);
            throw new SystemException(SystemErrorCode.E_DUPLICATE_REQUEST);
        }
        return point.proceed();
    }

    private String buildKey(ProceedingJoinPoint point, Idempotent idempotent) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        String baseKey =
                idempotent.keyPrefix().isEmpty()
                        ? signature.getDeclaringTypeName() + "." + signature.getName()
                        : idempotent.keyPrefix();
        String userId = getCurrentUserId();
        String argsHash = hashArgs(point.getArgs());
        return "idempotent:" + baseKey + ":" + userId + ":" + argsHash;
    }

    @SuppressWarnings("deprecation")
    private String hashArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "noargs";
        }
        String joined = Arrays.deepToString(args);
        return Hashing.sha256().hashString(joined, StandardCharsets.UTF_8).toString();
    }

    private String getCurrentUserId() {
        try {
            SystemLoginUser user = AuthenticationUtils.getSystemLoginUser();
            if (user != null) {
                return String.valueOf(user.getUserId());
            }
        } catch (Exception ignored) {
            // Fall through to IP-based key
        }
        RequestContext ctx = ScopedValueContext.getRequestContext();
        if (ctx != null && ctx.getOriginRequest() != null) {
            HttpServletRequest request = ctx.getOriginRequest();
            return "ip:" + request.getRemoteAddr();
        }
        return "anonymous";
    }
}
