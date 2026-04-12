package com.lesofn.appforge.common.error.example;

import com.lesofn.appforge.common.error.api.ErrorCode;
import com.lesofn.appforge.common.error.manager.ErrorManager;
import com.lesofn.appforge.common.error.system.SystemProjectModule;
import lombok.Getter;

/**
 * 基础错误码定义
 *
 * @author sofn
 * @version 1.0 Created at: 2018/8/3
 */
@Getter
public enum TestSystemErrorCode implements ErrorCode {

    SUCCESS(0, "ok"),
    SYSTEM_ERROR(1, "system error"),
    GET_ENUM_FAILED(2, "获取枚举类型失败, 枚举类：{}");

    private final int nodeNum;
    private final String msg;

    TestSystemErrorCode(int nodeNum, String msg) {
        this.nodeNum = nodeNum;
        this.msg = msg;
        ErrorManager.register(SystemProjectModule.INSTANCE, this);
    }

}
