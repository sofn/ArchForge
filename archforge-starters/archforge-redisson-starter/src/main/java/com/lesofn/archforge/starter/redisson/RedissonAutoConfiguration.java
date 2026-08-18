package com.lesofn.archforge.starter.redisson;

import java.time.Duration;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * Manual Redisson auto-configuration.
 *
 * <p>
 * Redisson's Spring Boot starter is based on Boot 3.x and is removed/incompatible with Boot 4.x,
 * so we create {@link RedissonClient} directly. When a {@link LettuceConnectionFactory} is
 * available (e.g. a Testcontainers-backed factory in the test profile) we reuse its actual host
 * and port instead of the static YAML values.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "arch-forge.redisson", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedissonAutoConfiguration {

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient(RedisConnectionFactory connectionFactory, Environment env) {
        String host;
        int port;
        if (connectionFactory instanceof LettuceConnectionFactory lettuce) {
            host = lettuce.getHostName();
            port = lettuce.getPort();
        } else {
            host = env.getProperty("spring.data.redis.host", "127.0.0.1");
            port = env.getProperty("spring.data.redis.port", Integer.class, 6379);
        }

        String password = env.getProperty("spring.data.redis.password");
        int database = env.getProperty("spring.data.redis.database", Integer.class, 0);
        Duration timeout = env.getProperty("spring.data.redis.timeout", Duration.class, Duration.ofSeconds(5));
        boolean ssl = env.getProperty("spring.data.redis.ssl", Boolean.class, false);

        int timeoutMs = (int) timeout.toMillis();

        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer();
        String protocol = ssl ? "rediss://" : "redis://";
        serverConfig.setAddress(protocol + host + ":" + port)
                .setDatabase(database)
                .setConnectTimeout(timeoutMs)
                .setTimeout(timeoutMs)
                .setRetryAttempts(3)
                .setConnectionMinimumIdleSize(2)
                .setConnectionPoolSize(16);
        if (password != null && !password.isBlank()) {
            serverConfig.setPassword(password);
        }
        return Redisson.create(config);
    }
}
