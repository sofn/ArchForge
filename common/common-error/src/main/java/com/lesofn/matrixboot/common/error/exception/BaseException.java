package com.lesofn.matrixboot.common.error.exception;

import com.lesofn.matrixboot.common.error.manager.ErrorInfo;
import com.lesofn.matrixboot.common.error.api.ErrorCode;
import com.lesofn.matrixboot.common.error.api.ProjectModule;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 16:41
 */
public abstract class BaseException extends Exception implements IErrorCodeException {

    final ErrorInfo errorInfo;

    protected BaseException(String message) {
        super(message);
        this.errorInfo = ErrorInfo.parse(message);
    }

    protected BaseException(String message, Throwable cause) {
        super(message, cause);
        this.errorInfo = ErrorInfo.parse(message);
    }

    protected BaseException(Throwable cause) {
        super(cause);
        this.errorInfo = ErrorInfo.parse(cause.getMessage());
    }

    protected BaseException(ErrorInfo errorInfo) {
        super(errorInfo.toString());
        this.errorInfo = errorInfo;
    }

    protected BaseException(ErrorCode errorCode) {
        this(ErrorInfo.parse(errorCode));
        ProjectModule.check(projectModule(), errorCode.projectModule());
    }

    protected BaseException(ErrorCode errorCode, Object... args) {
        this(ErrorInfo.parse(errorCode, args));
        ProjectModule.check(projectModule(), errorCode.projectModule());
    }

    @Override
    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }
}
