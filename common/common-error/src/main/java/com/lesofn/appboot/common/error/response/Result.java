package com.lesofn.appboot.common.error.response;

import com.lesofn.appboot.common.error.api.ErrorCode;
import com.lesofn.appboot.common.error.manager.ErrorInfo;
import com.lesofn.appboot.common.error.system.SystemErrorCode;
import lombok.Getter;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 18:34
 */
@Getter
public class Result<T> extends ErrorInfo {

    private T data;

    public Result(int code, String msg) {
        super(code, msg);
    }


    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>(SystemErrorCode.SUCCESS.getCode(), SystemErrorCode.SUCCESS.getMsg());
        result.data = data;
        return result;
    }

    public static <T> Result<T> ok(T data) {
        return success(data);
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMsg());
    }

    public static Result<String> error(Integer code, String msg) {
        return new Result<>(code, msg);
    }

}
