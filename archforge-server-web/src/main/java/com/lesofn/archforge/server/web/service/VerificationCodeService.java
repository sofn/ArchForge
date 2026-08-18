package com.lesofn.archforge.server.web.service;

import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import com.lesofn.archforge.server.web.errors.WebAuthErrorCode;
import com.lesofn.archforge.server.web.errors.WebAuthException;
import com.lesofn.archforge.server.web.mail.MailSender;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 邮箱验证码服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private static final String CODE_KEY_PREFIX = "verification:code:";
    private static final String LOCK_KEY_PREFIX = "verification:send:lock:";
    private static final String DAILY_COUNT_KEY_PREFIX = "verification:send:daily:";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final StringRedisTemplate redisTemplate;
    private final ArchForgeProperties archForgeConfig;
    private final MailSender mailSender;

    /**
     * 发送验证码。
     *
     * @param email 邮箱
     * @param purpose 用途
     * @return 是否成功发送
     */
    public boolean send(String email, VerificationCodePurpose purpose) {
        String lockKey = lockKey(email, purpose);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            Long ttlSeconds = redisTemplate.getExpire(lockKey);
            int waitSeconds = ttlSeconds == null ? archForgeConfig.getVerificationCode().getResendSeconds()
                    : Math.max(1, (int) ttlSeconds.longValue());
            throw new WebAuthException(WebAuthErrorCode.VERIFICATION_CODE_RESEND_TOO_FAST, waitSeconds);
        }

        int dailyMax = archForgeConfig.getVerificationCode().getDailyMaxPerEmail();
        if (dailyMax > 0) {
            String dailyKey = dailyCountKey(email, purpose);
            String countValue = redisTemplate.opsForValue().get(dailyKey);
            int count = countValue == null ? 0 : Integer.parseInt(countValue);
            if (count >= dailyMax) {
                throw new WebAuthException(WebAuthErrorCode.VERIFICATION_CODE_SEND_TOO_FREQUENT);
            }
        }

        String code = generateCode();
        String codeKey = codeKey(email, purpose);
        int expireSeconds = archForgeConfig.getVerificationCode().getExpireSeconds();
        int resendSeconds = archForgeConfig.getVerificationCode().getResendSeconds();

        redisTemplate.opsForValue().set(codeKey, code, Duration.ofSeconds(expireSeconds));
        redisTemplate.opsForValue().set(lockKey, "1", Duration.ofSeconds(resendSeconds));

        if (dailyMax > 0) {
            String dailyKey = dailyCountKey(email, purpose);
            Long current = redisTemplate.opsForValue().increment(dailyKey);
            if (current != null && current == 1L) {
                redisTemplate.expire(dailyKey, Duration.ofDays(1));
            }
        }

        try {
            mailSender.sendVerificationCode(email, code, purpose);
            log.info("Verification code sent, email={}, purpose={}, code={}", email, purpose, code);
            return true;
        } catch (Exception e) {
            log.error("Failed to send verification code, email={}, purpose={}", email, purpose, e);
            throw new WebAuthException("验证码发送失败，请稍后重试");
        }
    }

    /**
     * 校验验证码，校验通过后删除。
     *
     * @param email 邮箱
     * @param code 用户输入的验证码
     * @param purpose 用途
     * @return true 表示校验通过
     */
    public boolean verify(String email, String code, VerificationCodePurpose purpose) {
        String codeKey = codeKey(email, purpose);
        String storedCode = redisTemplate.opsForValue().get(codeKey);
        if (storedCode == null) {
            throw new WebAuthException(WebAuthErrorCode.VERIFICATION_CODE_INVALID);
        }
        if (!storedCode.equalsIgnoreCase(code)) {
            throw new WebAuthException(WebAuthErrorCode.VERIFICATION_CODE_INVALID);
        }
        redisTemplate.delete(codeKey);
        return true;
    }

    private String generateCode() {
        int code = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    private String codeKey(String email, VerificationCodePurpose purpose) {
        return CODE_KEY_PREFIX + purpose.name().toLowerCase() + ":" + email;
    }

    private String lockKey(String email, VerificationCodePurpose purpose) {
        return LOCK_KEY_PREFIX + purpose.name().toLowerCase() + ":" + email;
    }

    private String dailyCountKey(String email, VerificationCodePurpose purpose) {
        String date = LocalDate.now().format(DATE_FORMATTER);
        return DAILY_COUNT_KEY_PREFIX + purpose.name().toLowerCase() + ":" + date + ":" + email;
    }
}
