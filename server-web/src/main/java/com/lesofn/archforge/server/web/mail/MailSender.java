package com.lesofn.archforge.server.web.mail;

import com.lesofn.archforge.server.web.service.VerificationCodePurpose;

/**
 * 邮件发送端口。
 */
public interface MailSender {

    /**
     * 发送验证码邮件。
     *
     * @param email 收件人邮箱
     * @param code 验证码
     * @param purpose 验证码用途
     */
    void sendVerificationCode(String email, String code, VerificationCodePurpose purpose);
}
