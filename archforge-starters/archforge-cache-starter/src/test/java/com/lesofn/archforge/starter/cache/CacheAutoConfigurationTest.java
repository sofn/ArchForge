package com.lesofn.archforge.starter.cache;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;

/**
 * Integration tests for the cache starter auto-configuration.
 */
@SpringBootTest(classes = TestApplication.class)
@Import(RedisTestConfiguration.class)
@Tag("slow")
public class CacheAutoConfigurationTest {

    private final CacheManager cacheManager;
    private final RedissonClient redissonClient;
    private final CacheSyncBroadcaster cacheSyncBroadcaster;

    @Autowired
    public CacheAutoConfigurationTest(CacheManager cacheManager, RedissonClient redissonClient,
            CacheSyncBroadcaster cacheSyncBroadcaster) {
        this.cacheManager = cacheManager;
        this.redissonClient = redissonClient;
        this.cacheSyncBroadcaster = cacheSyncBroadcaster;
    }

    @AfterEach
    void cleanup() {
        redissonClient.getKeys().flushdb();
    }

    @Test
    void shouldCreateCompositeCacheManager() {
        assertThat(cacheManager).isInstanceOf(CompositeCacheManager.class);
    }

    @Test
    void shouldPutAndGetThroughCompositeCache() {
        Cache testCache = cacheManager.getCache("test");
        assertThat(testCache).isNotNull();

        testCache.put("key", "value");
        assertThat(testCache.get("key").get()).isEqualTo("value");

        testCache.evict("key");
        assertThat(testCache.get("key")).isNull();
    }

    @Test
    void shouldCreateCacheSyncBroadcaster() {
        assertThat(cacheSyncBroadcaster).isNotNull();
    }
}
