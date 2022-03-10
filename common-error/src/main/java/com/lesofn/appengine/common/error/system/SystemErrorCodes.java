package com.lesofn.appengine.common.error.system;

import com.lesofn.appengine.common.error.ErrorManager;
import com.lesofn.appengine.common.error.IErrorCode;
import lombok.Getter;

/**
 * 基础错误码定义
 *
 * @author lishafeng02
 * @date 2018/8/3
 */
@Getter
public enum SystemErrorCodes implements IErrorCode {

    SUCCESS(0, "ok"),
    SYSTEM_ERROR(1, "system error");

    private final int nodeNum;
    private final String msg;

    SystemErrorCodes(int nodeNum, String msg) {
        this.nodeNum = nodeNum;
        this.msg = msg;
        ErrorManager.register(SystemProjectModule.INSTANCE, this);
    }

}
