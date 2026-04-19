package com.lesofn.archsmith.infrastructure.auth.errors;

import com.lesofn.archsmith.common.error.api.ErrorCode;
import com.lesofn.archsmith.common.error.manager.ErrorManager;
import com.lesofn.archsmith.common.errors.ArchSmithProjectModule;
import lombok.Getter;

/**
 * 认证授权错误码定义
 *
 * @author sofn
 * @version 1.0 Created at: 2018/8/3
 */
@Getter
public enum AdminAuthErrorCode implements ErrorCode {
    USERNAME_PASSWORD_ERROR(1, "用户名或密码错误"),
    CAPTCHA_REQUIRED(2, "验证码不能为空"),
    CAPTCHA_EXPIRED(3, "验证码已失效"),
    CAPTCHA_ERROR(4, "验证码错误"),
    CAPTCHA_GENERATE_ERROR(5, "生成验证码异常"),
    DECODE_PASS_ERROR(6, "密码解密失败"),
    USER_PWD_TRYLIMIT(7, "用户名密码认证超过请求限制"),
    USER_AUTHFAIL(8, "认证失败"),
    TOKEN_EXPIRES(9, "Token 已过期"),
    TOKEN_INVALID(10, "Token 不合法"),
    REFRESH_TOKEN_INVALID(11, "Refresh Token 不合法"),
    INVALID_CLIENT(12, "不合法的客户端"),
    INVALID_REDIRECT_URL(13, "不合法的redirect url"),
    AUTHORIZE_CODE_ERROR(14, "获取 Authorize Code 错误"),
    UNSUPPORTED_RESPONSE_TYPE(15, "不支持的response type"),
    UNSUPPORTED_GRANT_TYPE(16, "不支持的grant type"),
    EMPTY_AUTHORIZE_CODE(17, "Authorize Code 不能为空"),
    EMPTY_USERNAME_PASSWORD(18, "用户名和密码不能为空"),
    EMPTY_REFRESH_TOKEN(19, "Refresh Token 不能为空"),
    ACCESS_TOKEN_ERROR(20, "获取Access Token错误"),
    AUTH_SECRET_ERROR(21, "客户端 Secret 错误"),
    NO_CLIENTID(22, "请上传client_id参数"),
    ILLEGAL_REQUEST(23, "不合法请求"),
    CLIENT_DISABLED(24, "client 被禁用"),
    ILLEGAL_GUEST(25, "不合法访客"),
    USER_RATE_LIMIT(26, "用户请求限制"),
    IP_RATE_LIMIT(27, "IP请求限制"),
    USER_IP_RATE_LIMIT(28, "用户和IP请求限制"),
    API_RATE_LIMIT(29, "Api请求限制"),

    USER_FAIL_TO_GET_USER_ID(30, "获取用户ID失败"),
    USER_FAIL_TO_GET_USER_INFO(31, "获取用户信息失败"),
    LOGIN_ERROR(32, "登录失败"),
    E_LOGIN_ACCOUNT_LOCKED(33, "账户已锁定，请 {} 分钟后重试"),
    E_PASSWORD_POLICY_VIOLATION(34, "密码不符合安全策略"),
    ;

    private final int nodeNum;
    private final String msg;

    AdminAuthErrorCode(int nodeNum, String msg) {
        this.nodeNum = nodeNum;
        this.msg = msg;
        ErrorManager.register(ArchSmithProjectModule.ADMIN_AUTH, this);
    }
}
