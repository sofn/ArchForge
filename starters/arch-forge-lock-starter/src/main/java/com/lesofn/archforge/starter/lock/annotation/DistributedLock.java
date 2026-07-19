package com.lesofn.archforge.starter.lock.annotation;

import com.lesofn.archforge.starter.lock.LockTimeoutStrategy;
import com.lesofn.archforge.starter.lock.LockType;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Annotation-driven distributed lock backed by Redisson.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /** Lock key; supports SpEL (e.g. {@code "order:#{id}"}). */
    String name();

    /** Lock type. */
    LockType lockType() default LockType.REENTRANT;

    /** Behaviour when the lock cannot be acquired within {@link #waitTime()}. */
    LockTimeoutStrategy timeoutStrategy() default LockTimeoutStrategy.FAIL;

    /** Maximum time to wait for the lock. */
    long waitTime() default 3;

    /**
     * Lease time. Default {@code -1} enables Redisson's watchdog, which keeps renewing
     * the lease while the method runs so the lock cannot expire mid-execution.
     */
    long leaseTime() default -1;

    /** Time unit for {@link #waitTime()} and {@link #leaseTime()}. */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
