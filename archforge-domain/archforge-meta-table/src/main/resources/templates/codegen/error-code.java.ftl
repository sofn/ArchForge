package ${packageBase}.error;

import com.lesofn.archforge.common.error.api.ErrorCode;
import com.lesofn.archforge.common.error.manager.ErrorManager;
import lombok.Getter;

@Getter
public enum ${entityName}ErrorCode implements ErrorCode {
    ${entityName?upper_case}_NOT_EXISTS(1, "${tableName}不存在"),
    ${entityName?upper_case}_DUPLICATE(2, "${tableName}已存在"),
    ${entityName?upper_case}_INVALID_STATUS(3, "${tableName}状态非法");

    private final int nodeNum;
    private final String msg;

    ${entityName}ErrorCode(int nodeNum, String msg) {
        this.nodeNum = nodeNum;
        this.msg = msg;
        ErrorManager.register(${entityName}ProjectModule.INSTANCE, this);
    }
}
