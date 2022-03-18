package com.lesofn.appengine.common.error.example;

import com.lesofn.appengine.common.error.manager.ErrorManager;
import com.lesofn.appengine.common.error.api.ErrorCode;
import lombok.Getter;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 16:19
 */
@Getter
public enum UserLoginErrorCodes implements ErrorCode {

    PARAM_INVALID(0, "ok");

    private final int nodeNum;
    private final String msg;

    UserLoginErrorCodes(int nodeNum, String msg) {
        this.nodeNum = nodeNum;
        this.msg = msg;
        ErrorManager.register(UserProjectCodes.LOGIN, this);
    }

}