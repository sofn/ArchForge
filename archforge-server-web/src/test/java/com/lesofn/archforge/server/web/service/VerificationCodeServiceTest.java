package com.lesofn.archforge.server.web.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import com.lesofn.archforge.server.web.errors.WebAuthErrorCode;
import com.lesofn.archforge.server.web.errors.WebAuthException;
import com.lesofn.archforge.server.web.mail.MailSender;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/** Unit tests for {@link VerificationCodeService} (send/verify business rules). */
@Tag("P0")
@ExtendWith(MockitoExtension.class)
class VerificationCodeServiceTest {

    private static final String EMAIL = "user@example.com";

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOps;
    @Mock
    private MailSender mailSender;

    private VerificationCodeService service;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        ArchForgeProperties properties = new ArchForgeProperties();
        service = new VerificationCodeService(redisTemplate, properties, mailSender);
    }

    @Test
    void sendStoresCodeLockAndDailyCountThenMailsIt() {
        assertTrue(service.send(EMAIL, VerificationCodePurpose.REGISTER));

        verify(valueOps).set(eq("verification:code:register:" + EMAIL), anyString(), eq(Duration.ofSeconds(300)));
        verify(valueOps).set(eq("verification:send:lock:register:" + EMAIL), eq("1"), eq(Duration.ofSeconds(60)));
        verify(valueOps).increment(dailyKey("register"));
        verify(mailSender).sendVerificationCode(eq(EMAIL), anyString(), eq(VerificationCodePurpose.REGISTER));
    }

    @Test
    void sendRejectsWhenResendLockActive() {
        when(redisTemplate.hasKey("verification:send:lock:register:" + EMAIL)).thenReturn(true);
        when(redisTemplate.getExpire("verification:send:lock:register:" + EMAIL)).thenReturn(30L);

        WebAuthException exception = assertThrows(
                WebAuthException.class,
                () -> service.send(EMAIL, VerificationCodePurpose.REGISTER));

        assertEquals("请 30 秒后重试", exception.getErrorInfo().getMsg());
        verify(mailSender, never()).sendVerificationCode(anyString(), anyString(), any());
    }

    @Test
    void sendRejectsWhenDailyLimitReached() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(valueOps.get(dailyKey("register"))).thenReturn("20");

        WebAuthException exception = assertThrows(
                WebAuthException.class,
                () -> service.send(EMAIL, VerificationCodePurpose.REGISTER));

        assertTrue(exception.getErrorInfo().getMsg().contains("今日发送次数已达上限"));
    }

    @Test
    void verifyAcceptsMatchingCodeCaseInsensitivelyAndDeletesIt() {
        when(valueOps.get("verification:code:register:" + EMAIL)).thenReturn("123456");

        assertTrue(service.verify(EMAIL, "123456", VerificationCodePurpose.REGISTER));
        verify(redisTemplate).delete("verification:code:register:" + EMAIL);
    }

    @Test
    void verifyRejectsWrongCodeWithoutDeleting() {
        when(valueOps.get("verification:code:register:" + EMAIL)).thenReturn("123456");

        WebAuthException exception = assertThrows(
                WebAuthException.class,
                () -> service.verify(EMAIL, "654321", VerificationCodePurpose.REGISTER));

        assertTrue(exception.getErrorInfo().getMsg().contains("验证码错误或已过期"));
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void verifyRejectsUnknownCode() {
        when(valueOps.get("verification:code:register:" + EMAIL)).thenReturn(null);

        assertThrows(
                WebAuthException.class,
                () -> service.verify(EMAIL, "123456", VerificationCodePurpose.REGISTER));
    }

    @Test
    void sendWrapsMailFailuresIntoWebAuthException() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        org.mockito.Mockito.doThrow(new RuntimeException("smtp down"))
                .when(mailSender)
                .sendVerificationCode(anyString(), anyString(), any());

        assertThrows(
                WebAuthException.class,
                () -> service.send(EMAIL, VerificationCodePurpose.RESET_PASSWORD));
    }

    private static String dailyKey(String purpose) {
        return "verification:send:daily:" + purpose + ":" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter
                .ofPattern("yyyyMMdd")) + ":" + EMAIL;
    }
}
