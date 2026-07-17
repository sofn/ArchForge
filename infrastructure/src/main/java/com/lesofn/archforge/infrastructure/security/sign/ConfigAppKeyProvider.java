package com.lesofn.archforge.infrastructure.security.sign;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * 默认基于配置的 {@link AppKeyProvider} 实现。
 *
 * @author sofn
 */
@RequiredArgsConstructor
public class ConfigAppKeyProvider implements AppKeyProvider {

    private final ApiSignProperties apiSignProperties;

    @Override
    public @Nullable String getSecret(String appKey) {
        if (appKey == null || appKey.isBlank()) {
            return null;
        }
        return apiSignProperties.getApps().get(appKey);
    }
}
