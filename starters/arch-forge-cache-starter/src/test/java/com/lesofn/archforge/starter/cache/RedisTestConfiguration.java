package com.lesofn.archforge.starter.cache;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers-backed Redis configuration that exposes a primary {@link LettuceConnectionFactory}.
 */
@Configuration
public class RedisTestConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RedisTestConfiguration.class);
    private static final int REDIS_PORT = 6379;
    private static final String REDIS_IMAGE = "redis:7-alpine";

    private GenericContainer<?> redisContainer;
    private String redisHost;
    private int redisPort;

    @PostConstruct
    public void startRedis() {
        log.info("Starting Redis container via Testcontainers...");
        redisContainer = new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
                .withExposedPorts(REDIS_PORT);
        redisContainer.start();
        redisPort = redisContainer.getMappedPort(REDIS_PORT);
        redisHost = redisContainer.getHost();
        log.info("Redis container started at {}:{}", redisHost, redisPort);
    }

    @PreDestroy
    public void stopRedis() {
        if (redisContainer != null && redisContainer.isRunning()) {
            log.info("Stopping Redis container");
            redisContainer.stop();
            log.info("Redis container stopped");
        }
    }

    @Bean
    @Primary
    public LettuceConnectionFactory lettuceConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(configuration);
    }
}
