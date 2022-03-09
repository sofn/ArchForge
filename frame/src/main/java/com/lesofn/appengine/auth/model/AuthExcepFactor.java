package com.lesofn.appengine.auth.model;


import com.lesofn.appengine.common.exception.ExcepFactor;
import com.lesofn.appengine.common.utils.GlobalConstants;
import org.springframework.http.HttpStatus;

/**
 * @author sofn
 */
public class AuthExcepFactor extends ExcepFactor {

    public static final AuthExcepFactor E_USER_AUTHFAIL = new AuthExcepFactor(
            HttpStatus.FORBIDDEN, 1, "auth faild!", "认证失败");

    public static final AuthExcepFactor E_AUTH_PASSWORD_ERROR = new AuthExcepFactor(
            HttpStatus.UNAUTHORIZED, 2, "username or password error!", "用户名或密码不正确");

    public static final AuthExcepFactor E_AUTH_TOKEN_EXPIRES = new AuthExcepFactor(
            HttpStatus.UNAUTHORIZED, 4, "token expires!", "Token 已过期");

    public static final AuthExcepFactor E_AUTH_TOKEN_INVALID = new AuthExcepFactor(
            HttpStatus.UNAUTHORIZED, 5, "invalid token", "Token 不合法");

    public static final AuthExcepFactor E_AUTH_REFRESH_TOKEN_INVALID = new AuthExcepFactor(
            HttpStatus.UNAUTHORIZED, 6, "invalid refresh token", "Refresh Token 不合法");

    public static final AuthExcepFactor E_AUTH_INVALID_Client = new AuthExcepFactor(
            HttpStatus.UNAUTHORIZED, 7, "invalid client", "不合法的客户端");

    public static final AuthExcepFactor E_AUTH_INVALID_REDIRECT_URL = new AuthExcepFactor(
            HttpStatus.UNAUTHORIZED, 8, "invalid redirect url", "不合法的redirect url");

    public static final AuthExcepFactor E_AUTH_AUTHORIZE_CODE_ERROR = new AuthExcepFactor(
            HttpStatus.UNAUTHORIZED, 9, "generate authorize code error", "获取 Authorize Code 错误");

    public static final AuthExcepFactor E_AUTH_UNSUPPORTED_RESPONSE_TYPE = new AuthExcepFactor(
            HttpStatus.UNAUTHORIZED, 10, "unsupported response type", "不支持的response type");

    public static final AuthExcepFactor E_AUTH_UNSUPPORTED_GRANT_TYPE = new AuthExcepFactor(
            HttpStatus.UNAUTHORIZED, 11, "unsupported grant type", "grant type");

    public static final AuthExcepFactor E_AUTH_EMPTY_AUTHORIZE_CODE_ERROR = new AuthExcepFactor(
            HttpStatus.UNAUTHORIZED, 12, "authorize code must not be empty", "Authorize Code 不能为空");

    public static final AuthExcepFactor E_AUTH_EMPTY_USERNAME_PASSWORD_ERROR = new AuthExcepFactor(
            HttpStatus.UNAUTHORIZED, 13, "username or password must not be empty", "用户名和密码不能为空");

    public static final AuthExcepFactor E_AUTH_EMPTY_REFRESH_TOKEN_ERROR = new AuthExcepFactor(
            HttpStatus.UNAUTHORIZED, 14, "refresh token must not be empty", "Refresh Token 不能为空");

    public static final AuthExcepFactor E_AUTH_ACCESS_TOKEN_ERROR = new AuthExcepFactor(
            HttpStatus.UNAUTHORIZED, 15, "generate access token error", "获取Access Token错误");

    public static final AuthExcepFactor E_AUTH_SECRET_ERROR = new AuthExcepFactor(
            HttpStatus.UNAUTHORIZED, 16, "auth secret error", "客户端 Secret 错误");

    public static final AuthExcepFactor E_AUTH_NO_CLIENTID = new AuthExcepFactor(
            HttpStatus.UNAUTHORIZED, 17, "please add param client_id", "请上传client_id参数");

    public static final AuthExcepFactor E_AUTH_ILLEGAL_REQUEST = new AuthExcepFactor(
            HttpStatus.UNAUTHORIZED, 18, "illegal request", "不合法请求");

    public static final AuthExcepFactor E_AUTH_CLIENT_DISABLED = new AuthExcepFactor(
            HttpStatus.UNAUTHORIZED, 19, "client disabled", "client 被禁用");

    public static final AuthExcepFactor E_ILLEGAL_GUEST = new AuthExcepFactor(
            HttpStatus.UNAUTHORIZED, 20, "illegal guest", "不合法访客");

    public static final AuthExcepFactor E_USER_RATE_LIMIT = new AuthExcepFactor(
            HttpStatus.FORBIDDEN, 21, "User request limit", "用户请求限制");

    public static final AuthExcepFactor E_IP_RATE_LIMIT = new AuthExcepFactor(
            HttpStatus.FORBIDDEN, 22, "IP request limit", "IP请求限制");

    public static final AuthExcepFactor E_USER_IP_RATE_LIMIT = new AuthExcepFactor(
            HttpStatus.FORBIDDEN, 23, "User and IP request limit", "用户和IP请求限制");

    public static final AuthExcepFactor E_API_RATE_LIMIT = new AuthExcepFactor(
            HttpStatus.FORBIDDEN, 24, "Api request limit", "Api请求限制");

    /**
     * 用户名密码尝试受限
     */
    public static final AuthExcepFactor E_USER_PWD_TRYLIMIT = new AuthExcepFactor(
            HttpStatus.FORBIDDEN, 3,
            "Username and pwd auth out of rate limit!", "用户名密码认证超过请求限制!");

    AuthExcepFactor(HttpStatus httpStatus,
                    int errorCode, String errorMsg, String errorMsgCn) {
        super(GlobalConstants.SERVICE_ID_AUTH, httpStatus, errorCode, errorMsg, errorMsgCn);
    }

}
