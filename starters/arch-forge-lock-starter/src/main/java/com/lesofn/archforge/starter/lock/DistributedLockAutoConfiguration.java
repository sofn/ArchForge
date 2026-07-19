package com.lesofn.archforge.starter.lock;

import com.lesofn.archforge.starter.lock.aspect.DistributedLockAspect;
import com.lesofn.archforge.starter.redisson.RedissonAutoConfiguration;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Auto-configuration for distributed locking.
 */
@AutoConfiguration(after = RedissonAutoConfiguration.class)
@ConditionalOnProperty(prefix = "arch-forge.lock", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(RedissonClient.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class DistributedLockAutoConfiguration {

    @Bean
    public DistributedLockAspect distributedLockAspect(RedissonClient redissonClient) {
        return new DistributedLockAspect(redissonClient);
    }

    @Bean
    public DistributedLockService distributedLockService(RedissonClient redissonClient) {
        return new DistributedLockService(redissonClient);
    }
}
