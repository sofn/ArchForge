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
        public static final ExcepFactor COMMON_REQUEST_UNAUTHORIZED = CustomExcepFactor.COMMON_REQUEST_UNAUTHORIZED;
        public static final ExcepFactor COMMON_REQUEST_FORBIDDEN = CustomExcepFactor.COMMON_REQUEST_FORBIDDEN;
    }
}