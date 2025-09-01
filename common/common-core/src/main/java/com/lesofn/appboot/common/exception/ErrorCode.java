package com.lesofn.appboot.common.exception;

import com.lesofn.appboot.common.errors.ExcepFactor;

/**
 * 错误码枚举类
 * 
 * @author lesofn
 */
public class ErrorCode {

    public static class Client {
        public static final ExcepFactor INVALID_TOKEN = CustomExcepFactor.INVALID_TOKEN;
        public static final ExcepFactor TOKEN_PROCESS_FAILED = CustomExcepFactor.TOKEN_PROCESS_FAILED;
    }
}