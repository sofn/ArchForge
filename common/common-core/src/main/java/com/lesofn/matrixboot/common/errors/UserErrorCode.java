package com.lesofn.matrixboot.common.errors;

import com.lesofn.matrixboot.common.error.api.ErrorCode;
import com.lesofn.matrixboot.common.error.manager.ErrorManager;
import lombok.Getter;

/**
 * 基础错误码定义
 *
 * @author lishafeng02
 * @date 2018/8/3
 */
@Getter
public enum UserErrorCode implements ErrorCode {

    USER_NON_EXIST(1, "用户不存在"),
    USER_IS_DISABLE(2, "用户已被停用");

    private final int nodeNum;
    private final String msg;

    UserErrorCode(int nodeNum, String msg) {
        this.nodeNum = nodeNum;
        this.msg = msg;
        ErrorManager.register(MaxtrixBootProjectModule.USER, this);
    }

}
