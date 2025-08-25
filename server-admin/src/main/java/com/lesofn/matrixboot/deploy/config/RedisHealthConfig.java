package com.lesofn.matrixboot.deploy.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 自定义 Redis 健康检查配置
 * 用于解决 jedis-mock 在开发环境下的健康检查问题
 * 
 * @author sofn
 * @version 1.0 Created at: 2025-08-25
 */
@Slf4j
@Configuration
public class RedisHealthConfig {

    /**
     * 开发环境下的 Redis 健康检查器
     * 使用简单的 PING 命令来检查 Redis 连接状态
     */
    @Bean
    @Primary
    @Profile("dev")
    public HealthIndicator redisHealthIndicator(RedisConnectionFactory connectionFactory) {
        return new HealthIndicator() {
            @Override
            public Health health() {
                try {
                    RedisConnection connection = connectionFactory.getConnection();
                    String pong = connection.ping();
                    connection.close();
                    
                    if ("PONG".equals(pong)) {
                        return Health.up()
                                .withDetail("redis", "Available")
                                .withDetail("mode", "mock")
                                .build();
                    } else {
                        return Health.down()
                                .withDetail("redis", "Ping failed")
                                .build();
                    }
                } catch (Exception e) {
                    log.debug("Redis health check failed", e);
                    return Health.down()
                            .withDetail("redis", "Not available")
                            .withDetail("error", e.getMessage())
                            .build();
                }
            }
        };
    }
}