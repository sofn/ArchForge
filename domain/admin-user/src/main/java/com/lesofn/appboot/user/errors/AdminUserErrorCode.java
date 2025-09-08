package com.lesofn.appboot.user.errors;

import com.lesofn.appboot.common.error.api.ErrorCode;
import com.lesofn.appboot.common.error.manager.ErrorManager;
import com.lesofn.appboot.common.errors.AppBootProjectModule;
import lombok.Getter;

/**
 * 基础错误码定义
 *
 * @author sofn
 * @version 1.0 Created at: 2018/8/3
 */
@Getter
public enum AdminUserErrorCode implements ErrorCode {

    USER_NON_EXIST(1, "用户不存在"),
    USER_IS_DISABLE(2, "用户已被停用"),
    ACCOUNT_EXISTS(3, "账号已存在"),
    ACCOUNT_NOT_EXISTS(4, "账号不存在");

    private final int nodeNum;
    private final String msg;

    AdminUserErrorCode(int nodeNum, String msg) {
        this.nodeNum = nodeNum;
        this.msg = msg;
        ErrorManager.register(AppBootProjectModule.ADMIN_USER, this);
    }

}
