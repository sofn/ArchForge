package com.lesofn.appforge.infrastructure.config.jackson;

import com.lesofn.appforge.infrastructure.config.AppForgeConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据脱敏配置
 *
 * <p>当 app-forge.sensitive.enabled=true 时，注册脱敏Jackson模块
 *
 * @author sofn
 */
@Configuration
@ConditionalOnProperty(
        prefix = "app-forge.sensitive",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class SensitiveConfig {

    @Bean
    public SensitiveJacksonModule sensitiveJacksonModule(AppForgeConfig appForgeConfig) {
        return new SensitiveJacksonModule();
    }
}
