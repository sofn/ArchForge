package com.lesofn.archforge.server.admin.service.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.auth.stp.LoginSessionKeys;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import com.lesofn.archforge.infrastructure.user.web.RoleInfo;
import com.lesofn.archforge.server.admin.service.cache.RedisCacheService;
import com.lesofn.archforge.server.admin.service.cache.RedisCacheTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TokenServiceTest {

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private RedisCacheTemplate<String> refreshTokenCache;

    @Mock
    private AdminLoginUserFactory adminLoginUserFactory;

    @Spy
    private ArchForgeProperties appForgeConfig = new ArchForgeProperties();

    @InjectMocks
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        SaTokenContextMockUtil.setMockContext();
        when(redisCacheService.getRefreshTokenCache()).thenReturn(refreshTokenCache);
    }

    @AfterEach
    void tearDown() {
        StpAdminUtil.logout();
        SaTokenContextMockUtil.clearContext();
    }

    @Test
    void createTokenPutsUserInSaTokenSession() {
        SystemLoginUser loginUser = new SystemLoginUser(1L, false, "admin", "pass", RoleInfo.EMPTY_ROLE, 1L);

        String token = tokenService.createTokenAndPutUserInCache(loginUser);

        assertNotNull(token);
        SystemLoginUser stored = (SystemLoginUser) StpAdminUtil.getSession().get(LoginSessionKeys.LOGIN_USER);
        assertEquals("admin", stored.getUsername());
        assertEquals(1L, stored.getUserId());
        assertNull(stored.getPassword());
    }

    @Test
    void createRefreshTokenStoresUserId() {
        SystemLoginUser loginUser = new SystemLoginUser(1L, false, "admin", "pass", RoleInfo.EMPTY_ROLE, 1L);

        String refreshToken = tokenService.createRefreshToken(loginUser);

        assertNotNull(refreshToken);
        assertEquals(32, refreshToken.length());
        verify(refreshTokenCache).set(eq(refreshToken), eq("1"));
    }

    @Test
    void getLoginUserByRefreshTokenReloadsUser() {
        SystemLoginUser user = new SystemLoginUser(1L, false, "admin", "pass", RoleInfo.EMPTY_ROLE, 1L);
        when(refreshTokenCache.get("refresh")).thenReturn("1");
        when(adminLoginUserFactory.loadByUserId(1L)).thenReturn(user);

        SystemLoginUser result = tokenService.getLoginUserByRefreshToken("refresh");

        assertEquals("admin", result.getUsername());
        assertEquals(1L, result.getUserId());
    }

    @Test
    void getLoginUserByRefreshTokenMissingReturnsNull() {
        when(refreshTokenCache.get("refresh")).thenReturn(null);
        assertNull(tokenService.getLoginUserByRefreshToken("refresh"));
    }

    @Test
    void removeRefreshTokenDeletesCache() {
        tokenService.removeRefreshToken("refresh");
        verify(refreshTokenCache).delete("refresh");
    }

    @Test
    void getExpireSecondsReturnsConfiguredValue() {
        assertEquals(604800L, tokenService.getExpireSeconds());
    }
}
