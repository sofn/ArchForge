package com.lesofn.matrixboot.common.error.manager;

import com.lesofn.matrixboot.common.error.api.ErrorCode;
import com.lesofn.matrixboot.common.error.system.SystemErrorCodes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.helpers.MessageFormatter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sofn
 * @version 1.0 Created at: 2021-05-27 11:51
 */
@AllArgsConstructor
public class ErrorInfo {
    static final Map<Integer, ErrorInfo> NO_PARAM_CODES_MAP = new ConcurrentHashMap<>();
    static final Map<String, ErrorInfo> ERROR_MSG_CODES_MAP = new ConcurrentHashMap<>();
    /**
     * 错误码
     */
    @Getter
    private final int code;
    /**
     * 返回错误信息 英文
     */
    @Getter
    private final String msg;


    public static ErrorInfo parse(String message) {
        return ERROR_MSG_CODES_MAP.computeIfAbsent(message, it -> new ErrorInfo(SystemErrorCodes.SYSTEM_ERROR.getCode(), message));
    }

    public static ErrorInfo parse(ErrorCode errorCode) {
        int code = errorCode.getCode();
        return NO_PARAM_CODES_MAP.computeIfAbsent(code, it -> new ErrorInfo(it, errorCode.getMsg()));
    }

    public static ErrorInfo parse(ErrorCode errorCode, Object... args) {
        String msg = MessageFormatter.arrayFormat(errorCode.getMsg(), args).getMessage();
        return new ErrorInfo(errorCode.getCode(), msg);
    }

    @Override
    public String toString() {
        return "code=" + code + ",msg=" + msg;
    }
}
