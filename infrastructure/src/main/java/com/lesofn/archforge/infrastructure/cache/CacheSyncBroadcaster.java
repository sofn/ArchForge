package com.lesofn.archforge.infrastructure.cache;

import jakarta.annotation.PostConstruct;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

/**
 * Keeps the L1 (Caffeine) tier coherent across instances.
 *
 * <p>
 * Every L1-affecting mutation is published on a Redis topic; each instance evicts its own
 * L1 on receipt and ignores its own messages so there is no broadcast loop. The shared L2
 * (Redis) tier is not touched on receipt.
 */
@Slf4j
public class CacheSyncBroadcaster {

    private static final String TOPIC = "arch:cache:l1-invalidation";

    private final RedissonClient redissonClient;
    private final CaffeineCacheManager caffeineCacheManager;
    private final String instanceId = UUID.randomUUID().toString();

    public CacheSyncBroadcaster(RedissonClient redissonClient, CaffeineCacheManager caffeineCacheManager) {
        this.redissonClient = redissonClient;
        this.caffeineCacheManager = caffeineCacheManager;
    }

    @PostConstruct
    public void subscribe() {
        RTopic topic = redissonClient.getTopic(TOPIC);
        topic.addListener(CacheInvalidationMessage.class, (MessageListener<CacheInvalidationMessage>) (channel, msg) -> {
            if (msg == null || instanceId.equals(msg.getOrigin())) {
                return;
            }
            applyLocally(msg);
        });
        log.info("[arch-cache] L1 invalidation broadcaster subscribed (instance {})", instanceId);
    }

    /** Evict a single key from every instance's L1. */
    public void broadcastEvict(String cacheName, Object key) {
        publish(new CacheInvalidationMessage(instanceId, cacheName, key, false));
    }

    /** Clear an entire named cache's L1 on every instance. */
    public void broadcastClear(String cacheName) {
        publish(new CacheInvalidationMessage(instanceId, cacheName, null, true));
    }

    private void publish(CacheInvalidationMessage msg) {
        try {
            redissonClient.getTopic(TOPIC).publish(msg);
        } catch (Exception e) {
            log.warn("[arch-cache] Failed to broadcast L1 invalidation for cache '{}': {}",
                    msg.getCacheName(), e.getMessage());
        }
    }

    private void applyLocally(CacheInvalidationMessage msg) {
        Cache l1 = caffeineCacheManager.getCache(msg.getCacheName());
        if (l1 == null) {
            return;
        }
        if (msg.isClear()) {
            l1.clear();
        } else {
            l1.evict(msg.getKey());
        }
    }
}
