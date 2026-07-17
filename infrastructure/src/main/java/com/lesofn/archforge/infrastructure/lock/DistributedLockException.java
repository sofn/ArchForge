package com.lesofn.archforge.infrastructure.lock;

/**
 * Thrown when a distributed lock cannot be acquired.
 */
public class DistributedLockException extends RuntimeException {

    public DistributedLockException(String message) {
        super(message);
    }

    public DistributedLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
