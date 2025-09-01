package com.lesofn.appboot.server.admin.service.cache;

import com.github.fppt.jedismock.RedisServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author sofn
 * @version 1.0 Created at: 2025-08-25 23:33
 */
@Slf4j
@Profile("dev")
@Component
public class InitRedisMockServer {

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;
    
    private RedisServer redisServer;

    @PostConstruct
    public void init() {
        try {
            log.info("Starting Redis Mock Server on port: {}", redisPort);
            redisServer = RedisServer.newRedisServer(redisPort);
            redisServer.start();
            log.info("Redis Mock Server started successfully on port: {}", redisPort);
        } catch (IOException e) {
            log.error("Failed to start Redis Mock Server", e);
            throw new RuntimeException("Failed to start Redis Mock Server", e);
        }
    }
    
    @PreDestroy
    public void destroy() {
        if (redisServer != null) {
            try {
                log.info("Stopping Redis Mock Server");
                redisServer.stop();
                log.info("Redis Mock Server stopped successfully");
            } catch (Exception e) {
                log.error("Error stopping Redis Mock Server", e);
            }
        }
    }
}
