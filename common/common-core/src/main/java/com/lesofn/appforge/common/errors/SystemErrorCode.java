package com.lesofn.appforge.common.errors;

import com.lesofn.appforge.common.error.api.ErrorCode;
import com.lesofn.appforge.common.error.manager.ErrorManager;
import com.lesofn.appforge.common.error.system.SystemProjectModule;
import lombok.Getter;

/**
 * 基础错误码定义
 *
 * @author sofn
 * @version 1.0 Created at: 2018/8/3
 */
@Getter
public enum SystemErrorCode implements ErrorCode {
    SUCCESS(0, "ok"),
    SYSTEM_ERROR(1, "system error"),
    GET_ENUM_FAILED(2, "获取枚举类型失败, 枚举类：{}"),

    // 系统级异常
    E_DEFAULT(3, "system error!"),
    E_SERVICE_UNAVAILABLE(4, "service unavailable!"),
    E_DEPENDENCE_SERVICE_UNAVAILABLE(5, "dependence service unavailable!"),
    E_SYSTEM_BUSY(6, "system is busy please retry!"),
    E_DIGEST_ERROR(7, "digest error"),
    E_API_NOT_EXIST(8, "Request Api not found!"),
    E_METHOD_ERROR(9, "HTTP METHOD is not suported for this request!"),
    E_API_DEPRECATED_ERROR(10, "implementation is deprecated."),
    E_UNSUPPORT_MEDIATYPE_ERROR(11, "unsupport mediatype (%s)"),
    E_PARAM_ERROR(12, "param error, see doc for more info."),
    E_ILLEGAL_REQUEST(13, "Illegal Request!"),
    E_PARAM_MISS_ERROR(14, "miss required parameter (%s), see doc for more info."),
    E_PARAM_INVALID_ERROR(
            15, "parameter (%s)'s value invalid,expect (%s), but get (%s), see doc for more info."),
    E_POST_BODY_LENGTH_LIMIT(16, "request boday length over limit."),
    E_INPUT_IMAGEERROR(17, "unsupported image type, only suport JPG, GIF, PNG!"),
    E_INPUT_IMAGESIZEERROR(18, "image size too large."),
    E_FORBID_RESUBMIT(19, "forbid resubmit."),
    E_CLIENT_VERSION_UNSUPPORT(20, "client version unsupport this feature."),
    E_IP_LIMIT(21, "IP limit!"),
    E_SOURCE_LEVEL_ERROR(22, "permission denied! Need a high level appkey!"),
    E_CLIENTID_ERROR(23, "Client not exists!"),
    E_IP_OUTOFLIMIT(24, "IP requests out of rate limit!"),
    E_USER_OUTOFLIMIT(25, "User requests out of rate limit!"),
    E_API_OUTOFLIMIT(26, "User requests for %s out of rate limit!"),
    E_USER_NOTOPEN(27, "invalid user!"),
    E_ENTITY_NOT_FOUND(28, "target entity not find."),
    E_FORBIDWORD(29, "content contain forbid word"),
    E_FORBID_OP(30, "forbid this operation."),
    SERVICE_IS_MAINTAIN(31, "service is maintain"),
    E_FROZEN_USER(32, "forbid this operation."),
    E_BAN_USER(33, "forbid this operation."),
    SOMETHING_ERROR(34, "service is maintain"),
    E_EXCLUSIVE_PARAMS_ERROR(35, "exclusive params error."),
    COMMON_REQUEST_FORBIDDEN(36, "common request forbidden");

    private final int nodeNum;
    private final String msg;

    SystemErrorCode(int nodeNum, String msg) {
        this.nodeNum = nodeNum;
        this.msg = msg;
        ErrorManager.register(SystemProjectModule.INSTANCE, this);
    }
}
