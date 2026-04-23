package com.lesofn.archsmith.server.admin.service.login;

import com.lesofn.archsmith.infrastructure.auth.errors.AdminAuthErrorCode;
import com.lesofn.archsmith.infrastructure.auth.errors.AdminAuthException;
import com.lesofn.archsmith.infrastructure.config.ArchSmithConfig;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 登录失败计数 + 锁定服务。
 *
 * <p>使用 Redis 计数器，失败 {@code arch-smith.login.max-attempts} 次后锁定 {@code
 * arch-smith.login.lockout-seconds} 秒。
 *
 * @author sofn
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final String KEY_PREFIX = "login:attempts:";

    private final StringRedisTemplate redisTemplate;
    private final ArchSmithConfig archSmithConfig;

    /** 检查账户是否已锁定，若已锁定抛出异常。 */
    public void checkNotLocked(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        String key = KEY_PREFIX + username;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return;
        }
        int attempts = Integer.parseInt(value);
        int maxAttempts = archSmithConfig.getLogin().getMaxAttempts();
        if (attempts >= maxAttempts) {
            int minutes = Math.max(1, archSmithConfig.getLogin().getLockoutSeconds() / 60);
            log.warn("Account [{}] is locked after {} failed attempts", username, attempts);
            throw new AdminAuthException(AdminAuthErrorCode.E_LOGIN_ACCOUNT_LOCKED, minutes);
        }
    }

    /** 记录一次登录失败，首次失败时设置 TTL。 */
    public void recordFailure(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        String key = KEY_PREFIX + username;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(
                    key, Duration.ofSeconds(archSmithConfig.getLogin().getLockoutSeconds()));
        }
        log.debug("Login failure recorded for [{}], count={}", username, count);
    }

    /** 登录成功后清除失败计数。 */
    public void clearAttempts(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        redisTemplate.delete(KEY_PREFIX + username);
    }
}
