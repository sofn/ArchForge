package com.lesofn.appforge.user.errors;

import com.lesofn.appforge.common.error.api.ErrorCode;
import com.lesofn.appforge.common.error.api.ProjectModule;
import com.lesofn.appforge.common.error.exception.BaseRuntimeException;
import com.lesofn.appforge.common.error.manager.ErrorInfo;
import com.lesofn.appforge.common.errors.AppForgeProjectModule;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 16:41
 */
public class AdminUserException extends BaseRuntimeException {

    public AdminUserException(String message) {
        super(message);
    }

    public AdminUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdminUserException(Throwable cause) {
        super(cause);
    }

    public AdminUserException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public AdminUserException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AdminUserException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    @Override
    public ProjectModule projectModule() {
        return AppForgeProjectModule.ADMIN_USER;
    }
}
