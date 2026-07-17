package com.lesofn.archforge.infrastructure.security.sign;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * API 签名配置。
 *
 * <p>
 * 示例：
 *
 * <pre>
 * arch-forge:
 *   security:
 *     sign:
 *       enabled: true
 *       apps:
 *         test-app: test-secret
 * </pre>
 *
 * @author sofn
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "arch-forge.security.sign")
public class ApiSignProperties {

    /** 是否启用 API 签名拦截 */
    private boolean enabled = true;

    /** 允许的签名时间偏移（秒） */
    private int timeoutSeconds = 300;

    /** nonce 在 Redis 中的防重放缓存时长（秒，默认时间窗口 2 倍） */
    private long nonceTtlSeconds = 600;

    /** 静态 appKey -> appSecret 映射 */
    private Map<String, String> apps = new HashMap<>();
}
