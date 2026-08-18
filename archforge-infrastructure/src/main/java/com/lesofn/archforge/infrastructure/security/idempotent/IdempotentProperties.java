package com.lesofn.archforge.infrastructure.security.idempotent;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 幂等 Token / 防重放配置。
 *
 * @author sofn
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "arch-forge.idempotent")
public class IdempotentProperties {

    /** 是否启用幂等能力 */
    private boolean enabled = true;

    /** Token 申请接口生成的幂等 Token 在 Redis 中的前缀 */
    private String tokenPrefix = "idem:token:";

    /** 幂等 Token 默认有效期（秒） */
    private long tokenExpireSeconds = 600;

    /** PARAM / HEADER 模式下幂等 key 前缀 */
    private String keyPrefix = "idem:lock:";

    /** 默认幂等 Token 请求头 */
    private String headerName = "X-Idempotent-Token";
}
