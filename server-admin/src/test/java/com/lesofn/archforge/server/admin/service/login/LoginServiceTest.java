package com.lesofn.archforge.server.admin.service.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.code.kaptcha.Producer;
import com.lesofn.archforge.common.encrypt.RsaEncrypter;
import com.lesofn.archforge.infrastructure.auth.errors.AdminAuthErrorCode;
import com.lesofn.archforge.infrastructure.auth.errors.AdminAuthException;
import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.config.ArchForgeConfig;
import com.lesofn.archforge.infrastructure.config.CaptchaType;
import com.lesofn.archforge.infrastructure.dictionary.EnumDictionaryRegistry;
import com.lesofn.archforge.infrastructure.user.web.RoleInfo;
import com.lesofn.archforge.server.admin.dto.CaptchaDTO;
import com.lesofn.archforge.server.admin.dto.ConfigDTO;
import com.lesofn.archforge.server.admin.dto.LoginCommand;
import com.lesofn.archforge.server.admin.service.cache.RedisCacheService;
import com.lesofn.archforge.server.admin.service.cache.RedisCacheTemplate;
import com.lesofn.archforge.user.api.domain.SysLoginLog;
import com.lesofn.archforge.user.api.service.SysLoginLogService;
import java.awt.image.BufferedImage;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoginServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private SysLoginLogService loginLogService;

    @Spy
    private ArchForgeConfig appForgeConfig = new ArchForgeConfig();

    @Mock
    private Environment environment;

    @Mock
    private EnumDictionaryRegistry enumDictionaryRegistry;

    @Mock(name = "captchaProducer")
    private Producer captchaProducer;

    @Mock(name = "captchaProducerMath")
    private Producer captchaProducerMath;

    @Mock
    private RedisCacheTemplate<String> captchaCache;

    @InjectMocks
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loginService, "captchaProducer", captchaProducer);
        ReflectionTestUtils.setField(loginService, "captchaProducerMath", captchaProducerMath);
        when(environment.matchesProfiles("prod")).thenReturn(false);
        when(enumDictionaryRegistry.asDictionaryDataMap()).thenReturn(Map.of());
        when(loginLogService.create(any(SysLoginLog.class))).thenReturn(new SysLoginLog());
    }

    @Test
    void login_CaptchaDisabled_Success() {
        appForgeConfig.getCaptcha().setEnabled(false);

        LoginCommand command = new LoginCommand();
        command.setUsername("admin");
        command.setPassword("plain");

        SystemLoginUser loginUser = new SystemLoginUser(1L, false, "admin", "encoded", RoleInfo.EMPTY_ROLE, 1L);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(loginUser);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenService.createTokenAndPutUserInCache(loginUser)).thenReturn("token");

        LoginService.LoginResult result = loginService.login(command);

        assertEquals("token", result.getToken());
        assertEquals(loginUser, result.getLoginUser());
        verify(loginAttemptService).clearAttempts("admin");
        verify(loginLogService).create(any(SysLoginLog.class));
    }

    @Test
    void login_BadCredentials_ThrowsAndRecordsFailure() {
        appForgeConfig.getCaptcha().setEnabled(false);

        LoginCommand command = new LoginCommand();
        command.setUsername("admin");
        command.setPassword("plain");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        AdminAuthException exception = assertThrows(AdminAuthException.class, () -> loginService.login(command));
        assertEquals(
                AdminAuthErrorCode.USERNAME_PASSWORD_ERROR.getCode(),
                exception.getErrorInfo().getCode());
        verify(loginAttemptService).recordFailure("admin");
    }

    @Test
    void login_CaptchaWrong_ThrowsCaptchaError() {
        appForgeConfig.getCaptcha().setEnabled(true);

        LoginCommand command = new LoginCommand();
        command.setUsername("admin");
        command.setPassword("plain");
        command.setCaptchaCodeKey("uuid");
        command.setCaptchaCode("wrong");

        when(redisCacheService.getCaptchaCache()).thenReturn(captchaCache);
        when(captchaCache.get("uuid")).thenReturn("correct");

        AdminAuthException exception = assertThrows(AdminAuthException.class, () -> loginService.login(command));
        assertEquals(
                AdminAuthErrorCode.CAPTCHA_ERROR.getCode(), exception.getErrorInfo().getCode());
        verify(captchaCache).delete("uuid");
    }

    @Test
    void login_CaptchaMissing_ThrowsCaptchaRequired() {
        appForgeConfig.getCaptcha().setEnabled(true);

        LoginCommand command = new LoginCommand();
        command.setUsername("admin");
        command.setPassword("plain");
        command.setCaptchaCodeKey("uuid");

        AdminAuthException exception = assertThrows(AdminAuthException.class, () -> loginService.login(command));
        assertEquals(
                AdminAuthErrorCode.CAPTCHA_REQUIRED.getCode(),
                exception.getErrorInfo().getCode());
    }

    @Test
    void login_CaptchaExpired_ThrowsCaptchaExpired() {
        appForgeConfig.getCaptcha().setEnabled(true);

        LoginCommand command = new LoginCommand();
        command.setUsername("admin");
        command.setPassword("plain");
        command.setCaptchaCodeKey("uuid");
        command.setCaptchaCode("code");

        when(redisCacheService.getCaptchaCache()).thenReturn(captchaCache);
        when(captchaCache.get("uuid")).thenReturn(null);

        AdminAuthException exception = assertThrows(AdminAuthException.class, () -> loginService.login(command));
        assertEquals(
                AdminAuthErrorCode.CAPTCHA_EXPIRED.getCode(),
                exception.getErrorInfo().getCode());
    }

    @Test
    void generateCaptchaImg_Disabled_ReturnsEmptyDto() {
        appForgeConfig.getCaptcha().setEnabled(false);

        CaptchaDTO captcha = loginService.generateCaptchaImg();

        assertFalse(captcha.getIsCaptchaOn());
        assertTrue(captcha.getCaptchaCodeKey().isEmpty());
        assertTrue(captcha.getCaptchaCodeImg().isEmpty());
    }

    @Test
    void generateCaptchaImg_MathType_Success() {
        appForgeConfig.getCaptcha().setEnabled(true);
        appForgeConfig.setCaptchaType(CaptchaType.MATH);

        when(captchaProducerMath.createText()).thenReturn("1+1@2");
        when(captchaProducerMath.createImage("1+1"))
                .thenReturn(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
        when(redisCacheService.getCaptchaCache()).thenReturn(captchaCache);

        CaptchaDTO captcha = loginService.generateCaptchaImg();

        assertTrue(captcha.getIsCaptchaOn());
        assertNotNull(captcha.getCaptchaCodeKey());
        assertNotNull(captcha.getCaptchaCodeImg());
        verify(captchaCache).set(eq(captcha.getCaptchaCodeKey()), eq("2"));
    }

    @Test
    void generateCaptchaImg_CharType_Success() {
        appForgeConfig.getCaptcha().setEnabled(true);
        appForgeConfig.setCaptchaType(CaptchaType.CHAR);

        when(captchaProducer.createText()).thenReturn("abcd");
        when(captchaProducer.createImage("abcd"))
                .thenReturn(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
        when(redisCacheService.getCaptchaCache()).thenReturn(captchaCache);

        CaptchaDTO captcha = loginService.generateCaptchaImg();

        assertTrue(captcha.getIsCaptchaOn());
        assertNotNull(captcha.getCaptchaCodeKey());
        assertNotNull(captcha.getCaptchaCodeImg());
        verify(captchaCache).set(eq(captcha.getCaptchaCodeKey()), eq("abcd"));
    }

    @Test
    void getConfig_ReturnsCaptchaStatusAndDictionary() {
        appForgeConfig.getCaptcha().setEnabled(true);

        ConfigDTO config = loginService.getConfig();

        assertTrue(config.getIsCaptchaOn());
        assertNotNull(config.getDictionary());
    }

    @Test
    void decryptPassword_NonProdInvalidInput_FallsBackToPlain() {
        appForgeConfig.setRsaPrivateKey(null);

        String result = loginService.decryptPassword("plainPassword");

        assertEquals("plainPassword", result);
    }

    @Test
    void decryptPassword_Success() throws Exception {
        Map<String, String> keys = RsaEncrypter.generateKeyPair();
        appForgeConfig.setRsaPrivateKey(keys.get("privateKey"));
        String encrypted = RsaEncrypter.encrypt("secret", keys.get("publicKey"));

        String result = loginService.decryptPassword(encrypted);

        assertEquals("secret", result);
    }

    @Test
    void decryptPassword_ProdInvalidInput_ThrowsBadCredentialsException() {
        when(environment.matchesProfiles("prod")).thenReturn(true);

        assertThrows(BadCredentialsException.class, () -> loginService.decryptPassword("notEncrypted"));
    }
}
