package com.lesofn.archforge.starter.lock.aspect;

import com.lesofn.archforge.starter.lock.DistributedLockException;
import com.lesofn.archforge.starter.lock.LockTimeoutStrategy;
import com.lesofn.archforge.starter.lock.annotation.DistributedLock;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * AOP aspect for {@link DistributedLock}.
 */
@Slf4j
@Aspect
public class DistributedLockAspect {

    private static final String LOCK_KEY_PREFIX = "arch:lock:";

    private final RedissonClient redissonClient;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    public DistributedLockAspect(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = LOCK_KEY_PREFIX + resolveLockName(joinPoint, distributedLock.name());
        long waitTime = distributedLock.waitTime();
        long leaseTime = distributedLock.leaseTime();

        RLock lock = distributedLock.lockType().getLock(redissonClient, lockKey);
        boolean acquired = false;

        try {
            acquired = leaseTime <= 0
                    ? lock.tryLock(waitTime, distributedLock.timeUnit())
                    : lock.tryLock(waitTime, leaseTime, distributedLock.timeUnit());
            if (!acquired) {
                return switch (distributedLock.timeoutStrategy()) {
                    case RETURN_NULL -> {
                        log.warn("Lock [{}] not acquired within {} {}, skipping per RETURN_NULL strategy",
                                lockKey, waitTime, distributedLock.timeUnit());
                        yield null;
                    }
                    case FAIL -> throw new DistributedLockException("Failed to acquire distributed lock [" + lockKey +
                            "] within " + waitTime + " " + distributedLock.timeUnit());
                };
            }
            log.debug("Acquired distributed lock [{}]", lockKey);
            return joinPoint.proceed();
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Released distributed lock [{}]", lockKey);
            }
        }
    }

    private String resolveLockName(ProceedingJoinPoint joinPoint, String expression) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] paramNames = nameDiscoverer.getParameterNames(method);
        Object[] args = joinPoint.getArgs();

        if (paramNames == null || paramNames.length == 0) {
            return expression;
        }

        try {
            EvaluationContext context = new StandardEvaluationContext();
            for (int i = 0; i < paramNames.length; i++) {
                ((StandardEvaluationContext) context).setVariable(paramNames[i], args[i]);
            }
            Object value = parser.parseExpression(expression).getValue(context);
            return value != null ? value.toString() : expression;
        } catch (Exception e) {
            log.warn("Failed to parse SpEL expression [{}], using raw value", expression, e);
            return expression;
        }
    }
}
