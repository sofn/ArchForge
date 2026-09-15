package com.lesofn.archforge.cms.api.errors;

import com.lesofn.archforge.common.error.api.ErrorCode;
import com.lesofn.archforge.common.error.api.ProjectModule;
import com.lesofn.archforge.common.error.exception.BaseRuntimeException;
import com.lesofn.archforge.common.error.manager.ErrorInfo;
import com.lesofn.archforge.common.error.ArchForgeProjectModule;

public class CmsException extends BaseRuntimeException {

    public CmsException(String message) {
        super(message);
    }

    public CmsException(String message, Throwable cause) {
        super(message, cause);
    }

    public CmsException(Throwable cause) {
        super(cause);
    }

    public CmsException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public CmsException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CmsException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    @Override
    public ProjectModule projectModule() {
        return ArchForgeProjectModule.CMS;
    }
}
