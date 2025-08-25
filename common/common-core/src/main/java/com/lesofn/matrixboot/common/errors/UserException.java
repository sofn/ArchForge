package com.lesofn.matrixboot.common.errors;

import com.lesofn.matrixboot.common.error.api.ErrorCode;
import com.lesofn.matrixboot.common.error.api.ProjectModule;
import com.lesofn.matrixboot.common.error.exception.BaseRuntimeException;
import com.lesofn.matrixboot.common.error.manager.ErrorInfo;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 16:41
 */
public class UserException extends BaseRuntimeException {

    public UserException(String message) {
        super(message);
    }

    public UserException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserException(Throwable cause) {
        super(cause);
    }

    public UserException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UserException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    @Override
    public ProjectModule projectModule() {
        return MaxtrixBootProjectModule.USER;
    }
}
