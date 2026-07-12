package com.lesofn.archforge.server.admin.config;

import com.lesofn.archforge.infrastructure.config.ArchForgeConfig;
import com.lesofn.archforge.infrastructure.file.AdaptiveFileStorageService;
import com.lesofn.archforge.infrastructure.file.FileStorageService;
import com.lesofn.archforge.infrastructure.file.FileStorageServiceFactory;
import com.lesofn.archforge.user.api.domain.SysConfig;
import com.lesofn.archforge.user.api.service.SysConfigService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

/**
 * 启动时从 sys_config 读取文件存储配置并重新初始化 FileStorageService。
 *
 * <p>
 * sys_config 中未配置的项使用 application.yaml 中的默认值。修改参数后需要重启后端才能生效。
 *
 * @author sofn
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileStorageConfigInitializer implements SmartInitializingSingleton {

    private final ArchForgeConfig appForgeConfig;
    private final SysConfigService sysConfigService;
    private final FileStorageService fileStorageService;

    @Override
    public void afterSingletonsInstantiated() {
        ArchForgeConfig.FileStorage config = appForgeConfig.getFileStorage();
        config.setType(getString("file.storage.type", config.getType()));
        config.setLocalDir(getString("file.storage.localDir", config.getLocalDir()));

        ArchForgeConfig.S3Config s3 = config.getS3();
        s3.setEndpoint(getString("file.storage.s3.endpoint", s3.getEndpoint()));
        s3.setAccessKey(getString("file.storage.s3.accessKey", s3.getAccessKey()));
        s3.setSecretKey(getString("file.storage.s3.secretKey", s3.getSecretKey()));
        s3.setBucket(getString("file.storage.s3.bucket", s3.getBucket()));
        s3.setRegion(getString("file.storage.s3.region", s3.getRegion()));

        if (fileStorageService instanceof AdaptiveFileStorageService adaptive) {
            adaptive.setDelegate(FileStorageServiceFactory.create(config));
            log.info("File storage reconfigured from sys_config: type={}", config.getType());
        }
    }

    private String getString(String configKey, String defaultValue) {
        Optional<SysConfig> config = sysConfigService.findByConfigKey(configKey);
        return config.map(SysConfig::getConfigValue).filter(v -> !v.isBlank()).orElse(defaultValue);
    }
}
