package com.lesofn.appboot.user.errors;

import com.lesofn.appboot.common.error.api.ErrorCode;
import com.lesofn.appboot.common.error.api.ProjectModule;
import com.lesofn.appboot.common.error.exception.BaseRuntimeException;
import com.lesofn.appboot.common.error.manager.ErrorInfo;
import com.lesofn.appboot.common.errors.AppBootProjectModule;

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
        return AppBootProjectModule.ADMIN_USER;
    }
}
