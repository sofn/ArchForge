package com.lesofn.archforge.infrastructure.security;

import com.lesofn.archforge.common.error.ArchForgeProjectModule;
import com.lesofn.archforge.common.error.api.ErrorCode;
import com.lesofn.archforge.common.error.api.ProjectModule;
import com.lesofn.archforge.common.error.exception.BaseRuntimeException;

/**
 * 安全模块运行时异常，保证 projectModule 与 {@link SecurityErrorCode} 注册模块一致。
 */
public class SecurityException extends BaseRuntimeException {

    public SecurityException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SecurityException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    @Override
    public ProjectModule projectModule() {
        return ArchForgeProjectModule.ADMIN_AUTH;
    }
}
