package com.lesofn.matrixboot.common.error.system;

import com.lesofn.matrixboot.common.error.manager.ErrorManager;
import com.lesofn.matrixboot.common.error.api.ErrorCode;
import lombok.Getter;

/**
 * 基础错误码定义
 *
 * @author lishafeng02
 * @date 2018/8/3
 */
@Getter
public enum SystemErrorCode implements ErrorCode {

    SUCCESS(0, "ok"),
    SYSTEM_ERROR(1, "system error"),
    GET_ENUM_FAILED(2, "获取枚举类型失败, 枚举类：{}");

    private final int nodeNum;
    private final String msg;

    SystemErrorCode(int nodeNum, String msg) {
        this.nodeNum = nodeNum;
        this.msg = msg;
        ErrorManager.register(SystemProjectModule.INSTANCE, this);
    }

}
