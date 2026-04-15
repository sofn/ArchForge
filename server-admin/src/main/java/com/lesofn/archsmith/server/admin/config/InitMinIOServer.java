package com.lesofn.archsmith.server.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.MinIOContainer;

/**
 * 使用 Testcontainers 启动 MinIO 容器提供 S3 兼容存储。
 *
 * @author sofn
 */
@Slf4j
@Configuration
@Profile("dev")
@ConditionalOnProperty(name = "arch-smith.embedded.s3", havingValue = "true")
public class InitMinIOServer {

    private static final String MINIO_IMAGE = "minio/minio:latest";
    private static final String ACCESS_KEY = "minioadmin";
    private static final String SECRET_KEY = "minioadmin";

    private final MinIOContainer minioContainer;

    public InitMinIOServer() {
        try {
            log.info("Starting MinIO container via Testcontainers...");
            minioContainer =
                    new MinIOContainer(MINIO_IMAGE)
                            .withUserName(ACCESS_KEY)
                            .withPassword(SECRET_KEY);
            minioContainer.start();

            String endpoint = minioContainer.getS3URL();

            System.setProperty("arch-smith.file-storage.type", "s3");
            System.setProperty("arch-smith.file-storage.s3.endpoint", endpoint);
            System.setProperty("arch-smith.file-storage.s3.access-key", ACCESS_KEY);
            System.setProperty("arch-smith.file-storage.s3.secret-key", SECRET_KEY);

            log.info("MinIO container started at: {}", endpoint);
        } catch (Exception e) {
            log.error("Failed to start MinIO container", e);
            throw new RuntimeException("Failed to start MinIO container", e);
        }
    }

    @jakarta.annotation.PreDestroy
    public void destroy() {
        if (minioContainer != null && minioContainer.isRunning()) {
            log.info("Stopping MinIO container");
            minioContainer.stop();
        }
    }
}
