package com.lesofn.archforge.infrastructure.security.sign;

import org.jspecify.annotations.Nullable;

/**
 * 应用密钥提供者 SPI。
 *
 * <p>
 * 业务方可以实现本接口，从数据库、配置中心或缓存中读取 {@code appKey} 对应的
 * {@code appSecret}。若未提供自定义 Bean，则默认使用 {@link ApiSignProperties} 中的静态映射。
 *
 * @author sofn
 */
public interface AppKeyProvider {

    /**
     * 根据 appKey 获取 appSecret。
     *
     * @param appKey 应用标识
     * @return appSecret，不存在时返回 null
     */
    @Nullable
    String getSecret(String appKey);
}
