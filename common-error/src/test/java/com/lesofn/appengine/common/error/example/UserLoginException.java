package com.lesofn.appengine.common.error.example;

import com.lesofn.appengine.common.error.ErrorInfo;
import com.lesofn.appengine.common.error.IErrorCode;
import com.lesofn.appengine.common.error.IProjectModule;
import com.lesofn.appengine.common.error.exception.BaseException;

/**
 * @author lishaofeng
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

    protected UserLoginException(IErrorCode errorCode) {
        super(errorCode);
    }

    protected UserLoginException(IErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    @Override
    public IProjectModule projectModule() {
        return UserProjectCodes.LOGIN;
    }
}
