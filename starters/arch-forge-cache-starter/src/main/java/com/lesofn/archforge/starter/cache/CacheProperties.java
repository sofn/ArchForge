package com.lesofn.archforge.starter.cache;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the composite L1/L2 cache.
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "arch-forge.cache.composite")
public class CacheProperties {

    /** Whether the composite cache manager is enabled. */
    private boolean enabled = true;

    /** Whether L1 (Caffeine) cache is enabled. */
    private boolean l1Enabled = true;

    /** Whether L2 (Redis) cache is enabled. */
    private boolean l2Enabled = true;

    /** Caffeine initial capacity. */
    private int caffeineInitialCapacity = 128;

    /** Caffeine maximum size. */
    private long caffeineMaximumSize = 1024;

    /** Caffeine expire-after-write duration. */
    private Duration caffeineExpireAfterWrite = Duration.ofMinutes(5);

    /** Redis L2 default entry TTL. */
    private Duration redisTtl = Duration.ofMinutes(30);
}
