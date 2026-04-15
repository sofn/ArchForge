package com.lesofn.archsmith.infrastructure.auth.spi;

import com.lesofn.archsmith.infrastructure.auth.errors.AdminAuthException;
import com.lesofn.archsmith.infrastructure.auth.model.AuthRequest;

/**
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
     * @throws AdminAuthException
     */
    long auth(AuthRequest request) throws AdminAuthException;

    /** 认证后的额外检查 */
    void afterAuth(long uid, AuthRequest request) throws AdminAuthException;
}
