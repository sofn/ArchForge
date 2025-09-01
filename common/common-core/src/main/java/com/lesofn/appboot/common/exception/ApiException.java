package com.lesofn.appboot.common.exception;

import com.lesofn.appboot.common.errors.EngineException;
import com.lesofn.appboot.common.errors.ExcepFactor;

/**
 * API异常类
 * 
 * @author lesofn
 */
public class ApiException extends EngineException {

    private static final long serialVersionUID = 1L;

    public ApiException(Throwable cause, ExcepFactor factor) {
        super(factor, cause, factor.getErrorMsgCn());
    }

    public ApiException(Throwable cause, ExcepFactor factor, String errorMsgCn) {
        super(factor, cause, errorMsgCn);
    }

    public ApiException(ExcepFactor factor) {
        super(factor);
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(ExcepFactor factor, Object message) {
        super(factor, message);
    }
}