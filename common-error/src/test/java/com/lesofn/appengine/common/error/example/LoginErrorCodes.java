package com.lesofn.appengine.common.error.example;

import com.lesofn.appengine.common.error.api.ErrorCode;
import com.lesofn.appengine.common.error.manager.ErrorManager;
import lombok.Getter;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 16:19
 */
@Getter
public enum LoginErrorCodes implements ErrorCode {
    USER_NOT_EXIST(0, "用户名不存在"), //错误码: 10100
    PASSWORD_ERROR(1, "密码错误");    //错误码: 10101

    private final int nodeNum;
    private final String msg;

    LoginErrorCodes(int nodeNum, String msg) {
        this.nodeNum = nodeNum;
        this.msg = msg;
        ErrorManager.register(UserProjectCodes.LOGIN, this);
    }

}