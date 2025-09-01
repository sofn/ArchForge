package com.lesofn.appboot.common.exception;

import com.lesofn.appboot.common.errors.ExcepFactor;
import org.springframework.http.HttpStatus;

/**
 * 自定义错误码类
 * 
 * @author lesofn
 */
public class CustomExcepFactor extends ExcepFactor {
    
    protected CustomExcepFactor(int serviceId, HttpStatus httpStatus, int errorCode, String errorMsg, String errorMsgCn) {
        super(serviceId, httpStatus, errorCode, errorMsg, errorMsgCn);
    }
    
    public static final ExcepFactor INVALID_TOKEN = new CustomExcepFactor(0, HttpStatus.UNAUTHORIZED, 1,
            "Invalid token", "无效的令牌");
    
    public static final ExcepFactor TOKEN_PROCESS_FAILED = new CustomExcepFactor(0, HttpStatus.INTERNAL_SERVER_ERROR, 2,
            "Token process failed: %s", "令牌处理失败: %s");
}