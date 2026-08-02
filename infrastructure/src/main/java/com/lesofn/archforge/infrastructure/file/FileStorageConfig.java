package com.lesofn.archforge.infrastructure.file;

import com.lesofn.archforge.infrastructure.config.ArchForgeConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储配置，根据 arch-forge.file-storage.type 选择 local 或 s3 实现。
 *
 * <p>
 * 所有文件存储参数均通过 {@code arch-forge.file-storage.*} 配置项注入，不再从 sys_config 读取。
 *
 * @author sofn
 */
@Configuration
@RequiredArgsConstructor
public class FileStorageConfig {

    private final ArchForgeConfig appForgeConfig;

    @Bean
    public FileStorageService fileStorageService() {
        ArchForgeConfig.FileStorage config = appForgeConfig.getFileStorage();
        return new AdaptiveFileStorageService(FileStorageServiceFactory.create(config));
    }
}
