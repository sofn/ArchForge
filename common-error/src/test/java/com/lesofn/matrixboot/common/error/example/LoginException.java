package com.lesofn.matrixboot.common.error.example;

import com.lesofn.matrixboot.common.error.manager.ErrorInfo;
import com.lesofn.matrixboot.common.error.api.ErrorCode;
import com.lesofn.matrixboot.common.error.api.ProjectModule;
import com.lesofn.matrixboot.common.error.exception.BaseException;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 18:18
 */
public class LoginException extends BaseException {

    protected LoginException(String message) {
        super(message);
    }

    protected LoginException(String message, Throwable cause) {
        super(message, cause);
    }

    protected LoginException(Throwable cause) {
        super(cause);
    }

    protected LoginException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    protected LoginException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected LoginException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    @Override
    public ProjectModule projectModule() {
        return UserProjectCodes.LOGIN;
    }
}
