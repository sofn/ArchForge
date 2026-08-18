package com.lesofn.archforge.starter.lock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * Programmatic distributed lock API backed by Redisson.
 */
@Slf4j
public class DistributedLockService {

    private static final String LOCK_KEY_PREFIX = "arch:lock:";

    private final RedissonClient redissonClient;

    public DistributedLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public <T> T executeWithLock(String lockName, long waitTime, long leaseTime,
            TimeUnit timeUnit, Supplier<T> supplier) {
        return executeWithLock(lockName, LockType.REENTRANT, waitTime, leaseTime, timeUnit, supplier);
    }

    public <T> T executeWithLock(String lockName, LockType lockType, long waitTime, long leaseTime,
            TimeUnit timeUnit, Supplier<T> supplier) {
        String key = LOCK_KEY_PREFIX + lockName;
        RLock lock = lockType.getLock(redissonClient, key);
        boolean acquired = false;

        try {
            acquired = leaseTime <= 0
                    ? lock.tryLock(waitTime, timeUnit)
                    : lock.tryLock(waitTime, leaseTime, timeUnit);
            if (!acquired) {
                throw new DistributedLockException("Failed to acquire distributed lock [" + key + "] within " + waitTime + " " +
                        timeUnit);
            }
            log.debug("Acquired distributed lock [{}]", key);
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DistributedLockException("Lock acquisition interrupted for [" + key + "]", e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Released distributed lock [{}]", key);
            }
        }
    }

    public void executeWithLock(String lockName, long waitTime, long leaseTime,
            TimeUnit timeUnit, Runnable runnable) {
        executeWithLock(lockName, waitTime, leaseTime, timeUnit, () -> {
            runnable.run();
            return null;
        });
    }

    public <T> T executeWithLock(String lockName, Supplier<T> supplier) {
        return executeWithLock(lockName, 3, -1, TimeUnit.SECONDS, supplier);
    }

    public void executeWithLock(String lockName, Runnable runnable) {
        executeWithLock(lockName, 3, -1, TimeUnit.SECONDS, runnable);
    }

    public RLock tryLock(String lockName, long waitTime, long leaseTime, TimeUnit timeUnit) {
        return tryLock(lockName, LockType.REENTRANT, waitTime, leaseTime, timeUnit);
    }

    public RLock tryLock(String lockName, LockType lockType, long waitTime, long leaseTime, TimeUnit timeUnit) {
        String key = LOCK_KEY_PREFIX + lockName;
        RLock lock = lockType.getLock(redissonClient, key);
        try {
            boolean acquired = leaseTime <= 0
                    ? lock.tryLock(waitTime, timeUnit)
                    : lock.tryLock(waitTime, leaseTime, timeUnit);
            if (!acquired) {
                return null;
            }
            return lock;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DistributedLockException("Lock acquisition interrupted for [" + key + "]", e);
        }
    }
}
