package com.lesofn.archforge.infrastructure.file;

import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * 文件存储服务工厂，根据 {@link ArchForgeProperties.FileStorage} 创建对应实现。
 *
 * @author sofn
 */
@Slf4j
public final class FileStorageServiceFactory {

    private FileStorageServiceFactory() {
    }

    /**
     * 根据配置创建文件存储服务实现。
     *
     * @param config 文件存储配置
     * @return 文件存储服务实例
     */
    public static FileStorageService create(ArchForgeProperties.FileStorage config) {
        if ("s3".equalsIgnoreCase(config.getType())) {
            ArchForgeProperties.S3Config s3 = config.getS3();
            log.info("Using S3 file storage: endpoint={}, bucket={}", s3.getEndpoint(), s3.getBucket());
            return new S3FileStorageService(s3.getEndpoint(), s3.getAccessKey(), s3.getSecretKey(), s3.getBucket(), s3
                    .getRegion());
        } else {
            log.info("Using local file storage: dir={}", config.getLocalDir());
            return new LocalFileStorageService(config.getLocalDir());
        }
    }
}
