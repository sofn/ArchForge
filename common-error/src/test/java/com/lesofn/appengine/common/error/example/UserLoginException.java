package com.lesofn.appengine.common.error.example;

import com.lesofn.appengine.common.error.manager.ErrorInfo;
import com.lesofn.appengine.common.error.api.ErrorCode;
import com.lesofn.appengine.common.error.api.ProjectModule;
import com.lesofn.appengine.common.error.exception.BaseException;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 18:18
 */
public class UserLoginException extends BaseException {

    protected UserLoginException(String message) {
        super(message);
    }

    protected UserLoginException(String message, Throwable cause) {
        super(message, cause);
    }

    protected UserLoginException(Throwable cause) {
        super(cause);
    }

    protected UserLoginException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    protected UserLoginException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected UserLoginException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    @Override
    public ProjectModule projectModule() {
        return UserProjectCodes.LOGIN;
    }
}
