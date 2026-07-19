package com.lesofn.archforge.starter.cache;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(RedisTestConfiguration.class)
class CacheAutoConfigurationTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CacheSyncBroadcaster cacheSyncBroadcaster;

    @Test
    void shouldCreateCompositeCacheManager() {
        assertThat(cacheManager).isInstanceOf(CompositeCacheManager.class);
    }

    @Test
    void shouldPutAndGetThroughCompositeCache() {
        Cache cache = cacheManager.getCache("testCache");
        assertThat(cache).isNotNull();

        cache.put("key", "value");
        assertThat(cache.get("key").get()).isEqualTo("value");

        cache.evict("key");
        assertThat(cache.get("key")).isNull();
    }

    @Test
    void shouldCreateCacheSyncBroadcaster() {
        assertThat(cacheSyncBroadcaster).isNotNull();
    }
}
