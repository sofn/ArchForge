package com.lesofn.archsmith.infrastructure.config.jackson;

import com.lesofn.archsmith.infrastructure.config.ArchSmithConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据脱敏配置
 *
 * <p>当 arch-smith.sensitive.enabled=true 时，注册脱敏Jackson模块
 *
 * @author sofn
 */
@Configuration
@ConditionalOnProperty(
        prefix = "arch-smith.sensitive",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class SensitiveConfig {

    @Bean
    public SensitiveJacksonModule sensitiveJacksonModule(ArchSmithConfig appForgeConfig) {
        return new SensitiveJacksonModule();
    }
}
