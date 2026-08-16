package com.lesofn.archforge.server.web.errors;

import com.lesofn.archforge.common.error.ArchForgeProjectModule;
import com.lesofn.archforge.common.error.api.ErrorCode;
import com.lesofn.archforge.common.error.manager.ErrorManager;
import lombok.Getter;

/**
 * C 端 Web 认证相关错误码。
 */
@Getter
public enum WebAuthErrorCode implements ErrorCode {
    VERIFICATION_CODE_INVALID(1, "验证码错误或已过期"),
    VERIFICATION_CODE_RESEND_TOO_FAST(2, "请 {} 秒后重试"),
    VERIFICATION_CODE_SEND_TOO_FREQUENT(3, "该邮箱今日发送次数已达上限"),
    REGISTER_DISABLED(4, "注册功能已关闭"),
    EMAIL_ALREADY_REGISTERED(5, "邮箱已被注册"),
    EMAIL_NOT_REGISTERED(6, "邮箱未注册"),
    PASSWORD_WEAK(7, "密码至少 8 位，且包含大写字母、小写字母和数字"),
    PASSWORD_MISMATCH(8, "两次输入的密码不一致");

    private final int nodeNum;
    private final String msg;

    WebAuthErrorCode(int nodeNum, String msg) {
        this.nodeNum = nodeNum;
        this.msg = msg;
        ErrorManager.register(ArchForgeProjectModule.WEB_AUTH, this);
    }
}
