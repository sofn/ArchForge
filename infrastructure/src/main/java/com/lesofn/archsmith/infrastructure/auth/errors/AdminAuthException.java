package com.lesofn.archsmith.infrastructure.auth.errors;

import com.lesofn.archsmith.common.error.api.ErrorCode;
import com.lesofn.archsmith.common.error.api.ProjectModule;
import com.lesofn.archsmith.common.error.exception.BaseRuntimeException;
import com.lesofn.archsmith.common.error.manager.ErrorInfo;
import com.lesofn.archsmith.common.errors.ArchSmithProjectModule;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 16:41
 */
public class AdminAuthException extends BaseRuntimeException {

    public AdminAuthException(String message) {
        super(message);
    }

    public AdminAuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdminAuthException(Throwable cause) {
        super(cause);
    }

    public AdminAuthException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public AdminAuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AdminAuthException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    @Override
    public ProjectModule projectModule() {
        return ArchSmithProjectModule.ADMIN_AUTH;
    }
}
