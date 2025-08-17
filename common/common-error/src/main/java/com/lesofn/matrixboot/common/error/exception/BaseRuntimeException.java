package com.lesofn.matrixboot.common.error.exception;

import com.lesofn.matrixboot.common.error.manager.ErrorInfo;
import com.lesofn.matrixboot.common.error.api.ErrorCode;
import com.lesofn.matrixboot.common.error.api.ProjectModule;
import lombok.Getter;

/**
 * @author sofn
 * @version 1.0 Created at: 2021-05-27 11:34
 */
@Getter
public abstract class BaseRuntimeException extends RuntimeException implements IErrorCodeException{

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

    protected BaseRuntimeException(ErrorCode errorCode) {
        this(ErrorInfo.parse(errorCode));
        ProjectModule.check(projectModule(), errorCode.projectModule());
    }

    protected BaseRuntimeException(ErrorCode errorCode, Object... args) {
        this(ErrorInfo.parse(errorCode, args));
        ProjectModule.check(projectModule(), errorCode.projectModule());
    }

    @Override
    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }
}
