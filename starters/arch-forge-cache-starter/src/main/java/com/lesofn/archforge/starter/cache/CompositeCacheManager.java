package com.lesofn.archforge.starter.cache;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;

/**
 * Two-level composite cache manager (L1 Caffeine + L2 Redis).
 */
public class CompositeCacheManager implements CacheManager {

    private final CaffeineCacheManager caffeineCacheManager;
    private final RedisCacheManager redisCacheManager;
    private final CacheSyncBroadcaster broadcaster;

    public CompositeCacheManager(CaffeineCacheManager caffeineCacheManager,
            RedisCacheManager redisCacheManager,
            CacheSyncBroadcaster broadcaster) {
        this.caffeineCacheManager = caffeineCacheManager;
        this.redisCacheManager = redisCacheManager;
        this.broadcaster = broadcaster;
    }

    @Override
    public Cache getCache(String name) {
        Cache caffeineCache = caffeineCacheManager != null ? caffeineCacheManager.getCache(name) : null;
        Cache redisCache = redisCacheManager != null ? redisCacheManager.getCache(name) : null;
        if (caffeineCache == null && redisCache == null) {
            return null;
        }
        return new CompositeCache(name, caffeineCache, redisCache, broadcaster);
    }

    @Override
    public Collection<String> getCacheNames() {
        Set<String> names = new LinkedHashSet<>();
        if (caffeineCacheManager != null) {
            names.addAll(caffeineCacheManager.getCacheNames());
        }
        if (redisCacheManager != null) {
            names.addAll(redisCacheManager.getCacheNames());
        }
        return names;
    }
}
