package com.lesofn.archforge.server.web.mail;

import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import com.lesofn.archforge.server.web.service.VerificationCodePurpose;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * 基于 Spring {@link JavaMailSender} 的真实邮件发送实现。
 *
 * <p>
 * 开启条件：配置 {@code arch-forge.mail.enabled=true} 且存在 {@code spring.mail.host} 等标准配置。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "arch-forge.mail", name = "enabled", havingValue = "true")
@ConditionalOnBean(JavaMailSender.class)
public class SpringJavaMailSender implements MailSender {

    private final JavaMailSender javaMailSender;
    private final ArchForgeProperties archForgeConfig;

    @Override
    public void sendVerificationCode(String email, String code, VerificationCodePurpose purpose) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(archForgeConfig.getMail().getFrom());
            helper.setTo(email);
            helper.setSubject(buildSubject(purpose));
            helper.setText(buildText(code, purpose), true);
            javaMailSender.send(message);
            log.debug("Sent verification email to {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to {}", email, e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    private String buildSubject(VerificationCodePurpose purpose) {
        String base = archForgeConfig.getMail().getVerificationSubject();
        return switch (purpose) {
            case REGISTER -> base + " - 注册";
            case RESET_PASSWORD -> base + " - 重置密码";
        };
    }

    private String buildText(String code, VerificationCodePurpose purpose) {
        String action = switch (purpose) {
            case REGISTER -> "注册";
            case RESET_PASSWORD -> "重置密码";
        };
        return "<p>您正在" + action + " ArchForgeWeb 账号。</p>" + "<p>验证码：<strong>" + code + "</strong></p>" + "<p>请在 5 分钟内使用。</p>";
    }
}
