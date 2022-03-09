package com.lesofn.appengine.auth.annotation;

/**
 * @author sofn
 */
public enum AuthType {
    // 可选 如果有用户信息则验证，验证失败不报错
    OPTION(false),
    // 必须验证
    REQUIRED(true),
    // 外部认证，内部不需要
    OUTER(true),
    //访客模式，用户信息验证失败不报错，增加设备号验证
    GUEST(true);

    // 认证失败是否抛出异常
    private boolean authFailThrowException;

    AuthType(boolean authFailThrowException) {
        this.authFailThrowException = authFailThrowException;
    }

    public boolean authFailThrowException() {
        return this.authFailThrowException;
    }
}
