package com.lesofn.archsmith.server.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

/**
 * 使用 Testcontainers 启动 RustFS (S3 兼容) 容器提供对象存储。
 *
 * @author sofn
 */
@Slf4j
@Configuration
@Profile("dev")
@ConditionalOnProperty(name = "arch-smith.embedded.s3", havingValue = "true")
public class InitS3Server {

    private static final String RUSTFS_IMAGE = "rustfs/rustfs:latest";
    private static final int S3_PORT = 9000;
    private static final String ACCESS_KEY = "minioadmin";
    private static final String SECRET_KEY = "minioadmin";

    private final GenericContainer<?> s3Container;

    @SuppressWarnings("resource")
    public InitS3Server() {
        try {
            log.info("Starting RustFS (S3) container via Testcontainers...");
            s3Container =
                    new GenericContainer<>(RUSTFS_IMAGE)
                            .withExposedPorts(S3_PORT)
                            .withEnv("RUSTFS_ROOT_USER", ACCESS_KEY)
                            .withEnv("RUSTFS_ROOT_PASSWORD", SECRET_KEY)
                            .withCommand("server", "/data")
                            .waitingFor(new HttpWaitStrategy().forPort(S3_PORT).forPath("/health"));
            s3Container.start();

            String endpoint =
                    "http://" + s3Container.getHost() + ":" + s3Container.getMappedPort(S3_PORT);

            System.setProperty("arch-smith.file-storage.type", "s3");
            System.setProperty("arch-smith.file-storage.s3.endpoint", endpoint);
            System.setProperty("arch-smith.file-storage.s3.access-key", ACCESS_KEY);
            System.setProperty("arch-smith.file-storage.s3.secret-key", SECRET_KEY);

            log.info("RustFS (S3) container started at: {}", endpoint);
        } catch (Exception e) {
            log.error("Failed to start RustFS container", e);
            throw new RuntimeException("Failed to start RustFS container", e);
        }
    }

    @jakarta.annotation.PreDestroy
    public void destroy() {
        if (s3Container != null && s3Container.isRunning()) {
            log.info("Stopping RustFS (S3) container");
            s3Container.stop();
        }
    }
}
