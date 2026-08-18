package com.lesofn.archforge.starter.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * Distributed lock type. Each enum knows how to obtain the matching {@link RLock} from Redisson.
 */
public enum LockType {

    /** Reentrant lock (default) — same thread may re-acquire. */
    REENTRANT,

    /** Fair lock — requests are granted in arrival order. */
    FAIR,

    /** Read lock of a read/write lock — shared with other readers, exclusive with writers. */
    READ,

    /** Write lock of a read/write lock — exclusive. */
    WRITE;

    public RLock getLock(RedissonClient client, String key) {
        return switch (this) {
            case REENTRANT -> client.getLock(key);
            case FAIR -> client.getFairLock(key);
            case READ -> client.getReadWriteLock(key).readLock();
            case WRITE -> client.getReadWriteLock(key).writeLock();
        };
    }
}
