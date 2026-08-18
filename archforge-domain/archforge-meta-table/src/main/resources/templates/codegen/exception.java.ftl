package ${packageBase}.error;

import com.lesofn.archforge.common.error.api.ErrorCode;
import com.lesofn.archforge.common.error.api.ProjectModule;
import com.lesofn.archforge.common.error.exception.BaseRuntimeException;
import com.lesofn.archforge.common.error.manager.ErrorInfo;

public class ${entityName}Exception extends BaseRuntimeException {

    public ${entityName}Exception(String message) {
        super(message);
    }

    public ${entityName}Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public ${entityName}Exception(Throwable cause) {
        super(cause);
    }

    public ${entityName}Exception(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public ${entityName}Exception(ErrorCode errorCode) {
        super(errorCode);
    }

    public ${entityName}Exception(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    @Override
    public ProjectModule projectModule() {
        return ${entityName}ProjectModule.INSTANCE;
    }
}
