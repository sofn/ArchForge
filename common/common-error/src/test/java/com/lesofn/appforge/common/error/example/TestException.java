package com.lesofn.appforge.common.error.example;

import com.lesofn.appforge.common.error.api.ErrorCode;
import com.lesofn.appforge.common.error.api.ProjectModule;
import com.lesofn.appforge.common.error.exception.BaseException;
import com.lesofn.appforge.common.error.manager.ErrorInfo;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 18:18
 */
public class TestException extends BaseException {

    protected TestException(String message) {
        super(message);
    }

    protected TestException(String message, Throwable cause) {
        super(message, cause);
    }

    protected TestException(Throwable cause) {
        super(cause);
    }

    protected TestException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    protected TestException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected TestException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    @Override
    public ProjectModule projectModule() {
        return TestProjectCodes.LOGIN;
    }
}
