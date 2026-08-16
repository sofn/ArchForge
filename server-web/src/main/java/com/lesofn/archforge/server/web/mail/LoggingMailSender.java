package com.lesofn.archforge.server.web.mail;

import com.lesofn.archforge.server.web.service.VerificationCodePurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 默认邮件发送实现：将验证码输出到日志，不依赖真实 SMTP 服务。
 *
 * <p>
 * 当 {@code arch-forge.mail.enabled=false}（默认）时启用；生产环境可配置为 true 并启用 {@link SpringJavaMailSender}。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "arch-forge.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingMailSender implements MailSender {

    @Override
    public void sendVerificationCode(String email, String code, VerificationCodePurpose purpose) {
        log.info("[DEV MAIL] purpose={}, email={}, code={}", purpose, email, code);
    }
}
