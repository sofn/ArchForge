package com.lesofn.archforge.starter.lock;

/**
 * Strategy used when a distributed lock cannot be acquired within {@code waitTime}.
 */
public enum LockTimeoutStrategy {

    /** Throw {@link DistributedLockException} (default). */
    FAIL,

    /** Return {@code null} without executing the target method. */
    RETURN_NULL
}
