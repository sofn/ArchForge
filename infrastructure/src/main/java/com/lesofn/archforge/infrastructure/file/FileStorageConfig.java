package com.lesofn.archforge.infrastructure.file;

import com.lesofn.archforge.infrastructure.config.ArchForgeConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储配置，根据 arch-forge.file-storage.type 选择 local 或 s3 实现
 *
 * @author sofn
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class FileStorageConfig {

    private final ArchForgeConfig appForgeConfig;

    @Bean
    public FileStorageService fileStorageService() {
        ArchForgeConfig.FileStorage config = appForgeConfig.getFileStorage();
        if ("s3".equalsIgnoreCase(config.getType())) {
            ArchForgeConfig.S3Config s3 = config.getS3();
            log.info(
                    "Using S3 file storage: endpoint={}, bucket={}",
                    s3.getEndpoint(),
                    s3.getBucket());
            return new S3FileStorageService(
                    s3.getEndpoint(),
                    s3.getAccessKey(),
                    s3.getSecretKey(),
                    s3.getBucket(),
                    s3.getRegion());
        } else {
            log.info("Using local file storage: dir={}", config.getLocalDir());
            return new LocalFileStorageService(config.getLocalDir());
        }
    }
}
