package com.lesofn.archforge.starter.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Auto-configuration for the L1 (Caffeine) + L2 (Redis) composite cache.
 */
@AutoConfiguration(after = com.lesofn.archforge.starter.redisson.RedissonAutoConfiguration.class)
@ConditionalOnProperty(prefix = "arch-forge.cache.composite", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CacheProperties.class)
@EnableCaching
public class CacheAutoConfiguration {

    @Bean("caffeineCacheManager")
    @ConditionalOnProperty(prefix = "arch-forge.cache.composite", name = "l1-enabled", havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(name = "caffeineCacheManager")
    public CaffeineCacheManager caffeineCacheManager(CacheProperties cacheProperties) {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(cacheProperties.getCaffeineInitialCapacity())
                .maximumSize(cacheProperties.getCaffeineMaximumSize())
                .expireAfterWrite(cacheProperties.getCaffeineExpireAfterWrite()));
        manager.setAllowNullValues(true);
        return manager;
    }

    @Bean("redisCacheManager")
    @ConditionalOnProperty(prefix = "arch-forge.cache.composite", name = "l2-enabled", havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(name = "redisCacheManager")
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory,
            CacheProperties cacheProperties) {
        GenericJacksonJsonRedisSerializer valueSerializer = GenericJacksonJsonRedisSerializer.builder()
                .enableUnsafeDefaultTyping()
                .build();

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(cacheProperties.getRedisTtl())
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }

    @Bean
    @ConditionalOnBean(RedissonClient.class)
    @ConditionalOnProperty(prefix = "arch-forge.cache.composite", name = "l1-enabled", havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(CacheSyncBroadcaster.class)
    public CacheSyncBroadcaster cacheSyncBroadcaster(RedissonClient redissonClient,
            CaffeineCacheManager caffeineCacheManager) {
        return new CacheSyncBroadcaster(redissonClient, caffeineCacheManager);
    }

    @Bean
    @Primary
    public CacheManager cacheManager(CaffeineCacheManager caffeineCacheManager,
            RedisCacheManager redisCacheManager,
            CacheSyncBroadcaster cacheSyncBroadcaster) {
        return new CompositeCacheManager(caffeineCacheManager, redisCacheManager, cacheSyncBroadcaster);
    }
}
