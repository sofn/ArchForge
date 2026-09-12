package com.lesofn.archforge.infrastructure.auth.spi;

import com.lesofn.archforge.infrastructure.auth.errors.AdminAuthException;
import com.lesofn.archforge.common.auth.AuthRequest;

/**
 * 认证 SPI：各登录方式的统一校验入口。
 *
 * @author sofn
 */
public interface AuthSpi {

    String AUTH_HEADER = "Authorization";
    String COOKIE_NAME = "AUTH_COOKIE";

    String getName();

    boolean canAuth(AuthRequest request);

    /**
     * 验证失败则抛出异常
     *
     * @param request 上下文
     * @return uid
     * @throws AdminAuthException 认证失败时抛出
     */
    long auth(AuthRequest request) throws AdminAuthException;

    /** 认证后的额外检查 */
    void afterAuth(long uid, AuthRequest request) throws AdminAuthException;
}
