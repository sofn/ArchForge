package com.lesofn.archforge.server.web.errors;

import com.lesofn.archforge.common.error.ArchForgeProjectModule;
import com.lesofn.archforge.common.error.api.ErrorCode;
import com.lesofn.archforge.common.error.api.ProjectModule;
import com.lesofn.archforge.common.error.exception.BaseRuntimeException;
import com.lesofn.archforge.common.error.manager.ErrorInfo;

/**
 * C 端 Web 认证业务异常。
 */
public class WebAuthException extends BaseRuntimeException {

    public WebAuthException(String message) {
        super(message);
    }

    public WebAuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebAuthException(Throwable cause) {
        super(cause);
    }

    public WebAuthException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public WebAuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    public WebAuthException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    @Override
    public ProjectModule projectModule() {
        return ArchForgeProjectModule.WEB_AUTH;
    }
}
