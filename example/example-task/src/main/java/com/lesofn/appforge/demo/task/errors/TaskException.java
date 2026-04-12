package com.lesofn.appforge.demo.task.errors;

import com.lesofn.appforge.common.error.api.ErrorCode;
import com.lesofn.appforge.common.error.api.ProjectModule;
import com.lesofn.appforge.common.error.exception.BaseRuntimeException;
import com.lesofn.appforge.common.error.manager.ErrorInfo;
import com.lesofn.appforge.common.errors.AppForgeProjectModule;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 16:41
 */
public class TaskException extends BaseRuntimeException {

    public TaskException(String message) {
        super(message);
    }

    public TaskException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskException(Throwable cause) {
        super(cause);
    }

    public TaskException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public TaskException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TaskException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    @Override
    public ProjectModule projectModule() {
        return AppForgeProjectModule.TASK;
    }
}
