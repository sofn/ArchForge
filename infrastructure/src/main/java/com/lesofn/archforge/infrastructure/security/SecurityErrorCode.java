package com.lesofn.archforge.infrastructure.security;

import com.lesofn.archforge.common.error.ArchForgeProjectModule;
import com.lesofn.archforge.common.error.api.ErrorCode;
import com.lesofn.archforge.common.error.manager.ErrorManager;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 安全模块错误码（签名、幂等、数据权限）。
 *
 * <p>
 * 注册到 {@link ArchForgeProjectModule#ADMIN_AUTH}，nodeNum 从 40 起跳，避免与
 * {@link com.lesofn.archforge.infrastructure.auth.errors.AdminAuthErrorCode} 冲突。
 */
@Getter
@AllArgsConstructor
public enum SecurityErrorCode implements ErrorCode {

    SIGN_EXPIRED(40, "API signature expired"),
    REPLAY_ATTACK(41, "Replay attack detected (duplicate nonce)"),
    SIGN_INVALID(42, "API signature verification failed"),
    APP_KEY_NOT_FOUND(43, "App key not found or disabled"),
    IDEMPOTENT_TOKEN_MISSING(44, "Missing idempotent token"),
    IDEMPOTENT_TOKEN_INVALID(45, "Invalid or expired idempotent token"),
    IDEMPOTENT_REJECT(46, "Duplicate request rejected"),
    DATA_SCOPE_DENIED(47, "Data scope denied");

    private final int nodeNum;
    private final String msg;

    static {
        for (SecurityErrorCode code : values()) {
            ErrorManager.register(ArchForgeProjectModule.ADMIN_AUTH, code);
        }
    }
}
