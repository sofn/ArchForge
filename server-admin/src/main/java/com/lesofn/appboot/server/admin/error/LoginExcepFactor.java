package com.lesofn.appboot.server.admin.error;

import com.lesofn.appboot.common.errors.ExcepFactor;
import org.springframework.http.HttpStatus;

/**
 * 登录相关错误码
 *
 * @author lesofn
 */
public class LoginExcepFactor extends ExcepFactor {

    public static final LoginExcepFactor USERNAME_PASSWORD_ERROR = new LoginExcepFactor(
            HttpStatus.BAD_REQUEST, 1,
            "username or password error", "用户名或密码错误");

    public static final LoginExcepFactor CAPTCHA_REQUIRED = new LoginExcepFactor(
            HttpStatus.BAD_REQUEST, 2,
            "captcha is required", "验证码不能为空");

    public static final LoginExcepFactor CAPTCHA_EXPIRED = new LoginExcepFactor(
            HttpStatus.BAD_REQUEST, 3,
            "captcha expired", "验证码已失效");

    public static final LoginExcepFactor CAPTCHA_ERROR = new LoginExcepFactor(
            HttpStatus.BAD_REQUEST, 4,
            "captcha error", "验证码错误");

    public static final LoginExcepFactor CAPTCHA_GENERATE_ERROR = new LoginExcepFactor(
            HttpStatus.INTERNAL_SERVER_ERROR, 5,
            "generate captcha error", "生成验证码异常");

    protected LoginExcepFactor(HttpStatus httpStatus, int errorCode, String errorMsg, String errorMsgCn) {
        super(0, httpStatus, errorCode, errorMsg, errorMsgCn);
    }
}