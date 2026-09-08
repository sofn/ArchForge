package com.lesofn.archforge.common.utils.ip;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Bridges application.yaml ({@code arch-forge.ip-region.*}) into the static {@link XdbManager}.
 *
 * <p>
 * Values are optional: XdbManager falls back to its built-in defaults, so contexts that do
 * not scan this package (e.g. server-web) keep working with defaults.
 *
 * @author sofn
 */
@Component
public class IpRegionAutoConfiguration {

    @Value("${arch-forge.ip-region.v4-xdb-url:}")
    private String v4UrlTemplate;

    @Value("${arch-forge.ip-region.v6-xdb-url:}")
    private String v6UrlTemplate;

    @Value("${arch-forge.ip-region.cache-dir:}")
    private String cacheDir;

    @Value("${arch-forge.ip-region.offline-enabled:true}")
    private boolean offlineEnabled;

    @PostConstruct
    void init() {
        XdbManager.configure(v4UrlTemplate, v6UrlTemplate, cacheDir, offlineEnabled);
    }
}
