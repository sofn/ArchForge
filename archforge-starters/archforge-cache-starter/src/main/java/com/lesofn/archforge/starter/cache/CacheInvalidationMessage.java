package com.lesofn.archforge.starter.cache;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * Broadcast message used to keep L1 (Caffeine) caches coherent across JVM instances.
 *
 * <p>
 * Published on a Redis topic by {@link CacheSyncBroadcaster}; each instance evicts
 * its own local L1 entry on receipt. The shared L2 (Redis) tier is not touched.
 */
// Redis-deserialized: @NoArgsConstructor leaves fields unset for the serializer to fill.
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("NullAway.Init")
public class CacheInvalidationMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** UUID of the originating JVM; receivers ignore messages from themselves. */
    private String origin;

    /** Logical cache name (matches Spring cache name). */
    private String cacheName;

    /** Key to evict; ignored when <code>clear</code> is true. */
    private @Nullable Object key;

    /** When true, clear the entire named cache instead of a single key. */
    private boolean clear;
}
