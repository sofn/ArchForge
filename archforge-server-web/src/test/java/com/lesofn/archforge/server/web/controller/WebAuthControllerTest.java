package com.lesofn.archforge.server.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.infrastructure.annotation.RateLimit;
import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import com.lesofn.archforge.server.web.dto.WebLoginRequest;
import com.lesofn.archforge.server.web.service.VerificationCodeService;
import com.lesofn.archforge.server.web.service.WebRefreshTokenService;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.api.errors.AdminUserException;
import com.lesofn.archforge.user.api.service.SysUserService;
import com.lesofn.archforge.user.domain.adapter.port.PasswordEncoderPort;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Login hardening: IP rate limiting wired and no account-state leakage in failure messages. */
@ExtendWith(MockitoExtension.class)
class WebAuthControllerTest {

    private static final String GENERIC_FAILURE = "用户名或密码错误";

    @Mock
    private SysUserService sysUserService;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private WebRefreshTokenService webRefreshTokenService;

    @Mock
    private VerificationCodeService verificationCodeService;

    private WebAuthController controller;

    @BeforeEach
    void setUp() {
        controller = new WebAuthController(sysUserService, passwordEncoderPort, webRefreshTokenService, verificationCodeService, new ArchForgeProperties());
    }

    @Test
    void loginIsRateLimitedPerIpLikeAdminSide() throws Exception {
        Method login = WebAuthController.class.getMethod("login", WebLoginRequest.class);
        RateLimit rateLimit = login.getAnnotation(RateLimit.class);
        assertNotNull(rateLimit, "login must be protected by @RateLimit");
        assertEquals(RateLimit.LimitType.IP, rateLimit.limitType());
        assertEquals(60, rateLimit.time());
        assertEquals(5, rateLimit.maxCount());
    }

    @Test
    void disabledAccountDoesNotLeakAccountState() {
        SysUser disabled = new SysUser();
        disabled.setUserId(1L);
        disabled.setUsername("bob");
        disabled.disable();
        when(sysUserService.findByUsername("bob")).thenReturn(Optional.of(disabled));

        AdminUserException ex = assertThrows(
                AdminUserException.class, () -> controller.login(loginRequest("bob", "whatever")));

        assertEquals(GENERIC_FAILURE, ex.getMessage());
    }

    @Test
    void wrongPasswordUsesGenericMessage() {
        SysUser active = new SysUser();
        active.setUserId(1L);
        active.setUsername("bob");
        active.prepareForCreate("encoded");
        when(sysUserService.findByUsername("bob")).thenReturn(Optional.of(active));
        when(passwordEncoderPort.matches("wrong", "encoded")).thenReturn(false);

        AdminUserException ex = assertThrows(
                AdminUserException.class, () -> controller.login(loginRequest("bob", "wrong")));

        assertEquals(GENERIC_FAILURE, ex.getMessage());
    }

    @Test
    void unknownUserUsesGenericMessage() {
        when(sysUserService.findByUsername("ghost")).thenReturn(Optional.empty());
        when(sysUserService.findByEmail("ghost")).thenReturn(Optional.empty());

        AdminUserException ex = assertThrows(
                AdminUserException.class, () -> controller.login(loginRequest("ghost", "x")));

        assertEquals(GENERIC_FAILURE, ex.getMessage());
    }

    private WebLoginRequest loginRequest(String username, String password) {
        WebLoginRequest request = new WebLoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }
}
