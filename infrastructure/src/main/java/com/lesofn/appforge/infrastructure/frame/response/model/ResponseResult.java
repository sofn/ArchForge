package com.lesofn.appforge.infrastructure.frame.response.model;

import com.lesofn.appforge.common.error.api.ErrorCode;
import com.lesofn.appforge.common.error.manager.ErrorInfo;
import com.lesofn.appforge.common.errors.SystemErrorCode;
import lombok.Getter;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 18:34
 */
@Getter
public class ResponseResult<T> extends ErrorInfo {

    private T data;

    public ResponseResult(int code, String msg) {
        super(code, msg);
    }


    public static <T> ResponseResult<T> success(T data) {
        ResponseResult<T> result = new ResponseResult<>(SystemErrorCode.SUCCESS.getCode(), SystemErrorCode.SUCCESS.getMsg());
        result.data = data;
        return result;
    }

    public static <T> ResponseResult<T> error(ErrorCode errorCode) {
        return new ResponseResult<>(errorCode.getCode(), errorCode.getMsg());
    }

    public static ResponseResult<String> error(Integer code, String msg) {
        return new ResponseResult<>(code, msg);
    }

}
