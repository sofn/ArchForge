package com.lesofn.archsmith.server.admin.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 使用 Testcontainers 启动 Redis 容器，替代 jedis-mock，支持所有平台（包括 Apple Silicon Mac）。
 *
 * @author sofn
 * @version 2.0 Created at: 2025-08-25 23:33
 */
@Slf4j
@Configuration
@Profile("dev")
@ConditionalOnProperty(name = "arch-smith.embedded.redis", havingValue = "true")
public class InitRedisMockServer {

    private static final int REDIS_PORT = 6379;
    private static final String REDIS_IMAGE = "redis:7-alpine";

    private GenericContainer<?> redisContainer;
    private String redisHost;
    private Integer redisPort;

    public InitRedisMockServer() {
        try {
            log.info("Starting Redis container via Testcontainers...");
            redisContainer =
                    new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
                            .withExposedPorts(REDIS_PORT);
            redisContainer.start();

            redisPort = redisContainer.getMappedPort(REDIS_PORT);
            redisHost = redisContainer.getHost();
            log.info("Redis container started at {}:{}", redisHost, redisPort);
        } catch (Exception e) {
            log.error("Failed to start Redis container", e);
            throw new RuntimeException("Failed to start Redis container", e);
        }
    }

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration =
                new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(configuration);
    }

    @PreDestroy
    public void destroy() {
        if (redisContainer != null && redisContainer.isRunning()) {
            log.info("Stopping Redis container");
            redisContainer.stop();
            log.info("Redis container stopped");
        }
    }
}
