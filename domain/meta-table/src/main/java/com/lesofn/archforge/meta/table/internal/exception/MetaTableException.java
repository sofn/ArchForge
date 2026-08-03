package com.lesofn.archforge.meta.table.internal.exception;

import com.lesofn.archforge.common.error.ArchForgeProjectModule;
import com.lesofn.archforge.common.error.api.ErrorCode;
import com.lesofn.archforge.common.error.api.ProjectModule;
import com.lesofn.archforge.common.error.exception.BaseRuntimeException;
import com.lesofn.archforge.common.error.manager.ErrorInfo;

/**
 * 元表格业务异常。
 */
public class MetaTableException extends BaseRuntimeException {

    public MetaTableException(String message) {
        super(message);
    }

    public MetaTableException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetaTableException(Throwable cause) {
        super(cause);
    }

    public MetaTableException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public MetaTableException(ErrorCode errorCode) {
        super(errorCode);
    }

    public MetaTableException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    @Override
    public ProjectModule projectModule() {
        return ArchForgeProjectModule.META_TABLE;
    }
}
