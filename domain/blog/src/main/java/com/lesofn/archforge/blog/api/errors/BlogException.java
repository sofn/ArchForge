package com.lesofn.archforge.blog.api.errors;

import com.lesofn.archforge.common.error.api.ErrorCode;
import com.lesofn.archforge.common.error.api.ProjectModule;
import com.lesofn.archforge.common.error.exception.BaseRuntimeException;
import com.lesofn.archforge.common.error.manager.ErrorInfo;
import com.lesofn.archforge.common.error.ArchForgeProjectModule;

public class BlogException extends BaseRuntimeException {

    public BlogException(String message) {
        super(message);
    }

    public BlogException(String message, Throwable cause) {
        super(message, cause);
    }

    public BlogException(Throwable cause) {
        super(cause);
    }

    public BlogException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public BlogException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BlogException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    @Override
    public ProjectModule projectModule() {
        return ArchForgeProjectModule.BLOG;
    }
}
