package com.lesofn.appengine.common.error.example;

import com.lesofn.appengine.common.error.ErrorManager;
import com.lesofn.appengine.common.error.IErrorCode;
import lombok.Getter;

/**
 * @author lishaofeng
 * @version 1.0 Created at: 2022-03-09 16:19
 */
@Getter
public enum UserLoginErrorCodes implements IErrorCode {

    PARAM_INVALID(0, "ok");

    private final int nodeNum;
    private final String msg;

    UserLoginErrorCodes(int nodeNum, String msg) {
        this.nodeNum = nodeNum;
        this.msg = msg;
        ErrorManager.register(UserProjectCodes.LOGIN, this);
    }

}