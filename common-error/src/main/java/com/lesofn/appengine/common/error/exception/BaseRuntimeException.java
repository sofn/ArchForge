package com.lesofn.appengine.common.error.exception;

import com.lesofn.appengine.common.error.ErrorInfo;
import com.lesofn.appengine.common.error.IErrorCode;
import com.lesofn.appengine.common.error.IProjectModule;
import lombok.Getter;

/**
 * @author lishaofeng
 * @version 1.0 Created at: 2021-05-27 11:34
 */
@Getter
public abstract class BaseRuntimeException extends RuntimeException {

    final ErrorInfo errorInfo;

    protected BaseRuntimeException(String message) {
        super(message);
        this.errorInfo = ErrorInfo.parse(message);
    }

    protected BaseRuntimeException(String message, Throwable cause) {
        super(message, cause);
        this.errorInfo = ErrorInfo.parse(message);
    }

    protected BaseRuntimeException(Throwable cause) {
        super(cause);
        this.errorInfo = ErrorInfo.parse(cause.getMessage());
    }

    protected BaseRuntimeException(ErrorInfo errorInfo) {
        super(errorInfo.toString());
        this.errorInfo = errorInfo;
    }

    protected BaseRuntimeException(IErrorCode errorCode) {
        this(ErrorInfo.parse(errorCode));
        IProjectModule.check(projectModule(), errorCode.projectModule());
    }

    protected BaseRuntimeException(IErrorCode errorCode, Object... args) {
        this(ErrorInfo.parse(errorCode, args));
        IProjectModule.check(projectModule(), errorCode.projectModule());
    }

    protected abstract IProjectModule projectModule();
}
