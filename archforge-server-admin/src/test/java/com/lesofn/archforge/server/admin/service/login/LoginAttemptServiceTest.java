package com.lesofn.archforge.server.admin.service.login;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.infrastructure.auth.errors.AdminAuthException;
import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        ArchForgeProperties properties = new ArchForgeProperties();
        properties.getLogin().setMaxAttempts(3);
        properties.getLogin().setLockoutSeconds(600);
        loginAttemptService = new LoginAttemptService(redisTemplate, properties);
    }

    @Test
    void locksAfterMaxAttempts() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("login:attempts:admin")).thenReturn("3");
        assertThrows(AdminAuthException.class, () -> loginAttemptService.checkNotLocked("admin"));
    }

    @Test
    void allowsBelowThreshold() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("login:attempts:admin")).thenReturn("2");
        assertDoesNotThrow(() -> loginAttemptService.checkNotLocked("admin"));
    }

    @Test
    void firstFailureSetsTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("login:attempts:admin")).thenReturn(1L);
        loginAttemptService.recordFailure("admin");
        verify(redisTemplate).expire(eq("login:attempts:admin"), eq(Duration.ofSeconds(600)));
    }
}
