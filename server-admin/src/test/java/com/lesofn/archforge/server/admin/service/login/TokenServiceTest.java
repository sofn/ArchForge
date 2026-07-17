package com.lesofn.archforge.server.admin.service.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.common.constant.Constants;
import com.lesofn.archforge.infrastructure.auth.errors.AdminAuthException;
import com.lesofn.archforge.infrastructure.auth.errors.AdminAuthErrorCode;
import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.config.ArchForgeConfig;
import com.lesofn.archforge.infrastructure.user.web.RoleInfo;
import com.lesofn.archforge.server.admin.service.cache.RedisCacheService;
import com.lesofn.archforge.server.admin.service.cache.RedisCacheTemplate;
import com.lesofn.archforge.server.admin.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TokenServiceTest {

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RedisCacheTemplate<SystemLoginUser> loginUserCache;

    @Mock
    private RedisCacheTemplate<String> refreshTokenCache;

    @Spy
    private ArchForgeConfig appForgeConfig = new ArchForgeConfig();

    @Captor
    private ArgumentCaptor<Duration> durationCaptor;

    @InjectMocks
    private TokenService tokenService;

    @Test
    void getLoginUser_ValidToken_ReturnsUser() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("jti");
        when(claims.get(Constants.Token.LOGIN_USER_KEY)).thenReturn("uuid");
        when(jwtTokenUtil.parseToken("validToken")).thenReturn(claims);
        when(redisTemplate.hasKey("token:blacklist:jti")).thenReturn(false);

        SystemLoginUser user = new SystemLoginUser(1L, false, "admin", "pass", RoleInfo.EMPTY_ROLE, 1L);
        when(redisCacheService.getLoginUserCache()).thenReturn(loginUserCache);
        when(loginUserCache.get("uuid")).thenReturn(user);

        SystemLoginUser result = tokenService.getLoginUser(request);
        assertEquals(user, result);
    }

    @Test
    void getLoginUser_BlacklistedToken_ThrowsException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("jti");
        when(jwtTokenUtil.parseToken("validToken")).thenReturn(claims);
        when(redisTemplate.hasKey("token:blacklist:jti")).thenReturn(true);

        AdminAuthException exception = assertThrows(
                AdminAuthException.class, () -> tokenService.getLoginUser(request));
        assertEquals(
                AdminAuthErrorCode.TOKEN_INVALID.getCode(), exception.getErrorInfo().getCode());
    }

    @Test
    void getLoginUser_MalformedToken_ThrowsException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer badToken");
        when(jwtTokenUtil.parseToken("badToken"))
                .thenThrow(new MalformedJwtException("malformed"));

        AdminAuthException exception = assertThrows(
                AdminAuthException.class, () -> tokenService.getLoginUser(request));
        assertEquals(
                AdminAuthErrorCode.TOKEN_INVALID.getCode(), exception.getErrorInfo().getCode());
    }

    @Test
    void getLoginUser_NoToken_ReturnsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        SystemLoginUser result = tokenService.getLoginUser(request);
        assertNull(result);
    }

    @Test
    void createTokenAndPutUserInCache_Success() {
        SystemLoginUser loginUser = new SystemLoginUser(1L, false, "admin", "pass", RoleInfo.EMPTY_ROLE, 1L);
        when(jwtTokenUtil.generateToken(loginUser)).thenReturn("token");
        when(redisCacheService.getLoginUserCache()).thenReturn(loginUserCache);

        String token = tokenService.createTokenAndPutUserInCache(loginUser);

        assertEquals("token", token);
        assertNotNull(loginUser.getCachedKey());
        verify(loginUserCache).set(eq(loginUser.getCachedKey()), eq(loginUser));
    }

    @Test
    void refreshToken_WhenExpired_UpdatesCache() {
        SystemLoginUser loginUser = new SystemLoginUser(1L, false, "admin", "pass", RoleInfo.EMPTY_ROLE, 1L);
        loginUser.setCachedKey("key");
        loginUser.setAutoRefreshCacheTime(0L);
        when(redisCacheService.getLoginUserCache()).thenReturn(loginUserCache);

        tokenService.refreshToken(loginUser);

        assertTrue(loginUser.getAutoRefreshCacheTime() > 0);
        verify(loginUserCache).set(eq("key"), eq(loginUser));
    }

    @Test
    void refreshToken_WhenNotExpired_DoesNotUpdateCache() {
        SystemLoginUser loginUser = new SystemLoginUser(1L, false, "admin", "pass", RoleInfo.EMPTY_ROLE, 1L);
        loginUser.setCachedKey("key");
        loginUser.setAutoRefreshCacheTime(Long.MAX_VALUE);
        when(redisCacheService.getLoginUserCache()).thenReturn(loginUserCache);

        tokenService.refreshToken(loginUser);

        verify(loginUserCache, never()).set(anyString(), any(SystemLoginUser.class));
    }

    @Test
    void createRefreshToken_Success() {
        SystemLoginUser loginUser = new SystemLoginUser(1L, false, "admin", "pass", RoleInfo.EMPTY_ROLE, 1L);
        loginUser.setCachedKey("cachedKey");
        when(redisCacheService.getRefreshTokenCache()).thenReturn(refreshTokenCache);

        String refreshToken = tokenService.createRefreshToken(loginUser);

        assertNotNull(refreshToken);
        assertEquals(32, refreshToken.length());
        verify(refreshTokenCache).set(eq(refreshToken), eq("cachedKey"));
    }

    @Test
    void getLoginUserByRefreshToken_Success() {
        SystemLoginUser user = new SystemLoginUser(1L, false, "admin", "pass", RoleInfo.EMPTY_ROLE, 1L);
        when(redisCacheService.getRefreshTokenCache()).thenReturn(refreshTokenCache);
        when(refreshTokenCache.get("refresh")).thenReturn("cachedKey");
        when(redisCacheService.getLoginUserCache()).thenReturn(loginUserCache);
        when(loginUserCache.get("cachedKey")).thenReturn(user);

        SystemLoginUser result = tokenService.getLoginUserByRefreshToken("refresh");

        assertEquals(user, result);
    }

    @Test
    void removeRefreshToken_Success() {
        when(redisCacheService.getRefreshTokenCache()).thenReturn(refreshTokenCache);

        tokenService.removeRefreshToken("refresh");

        verify(refreshTokenCache).delete("refresh");
    }

    @Test
    void removeToken_Success() {
        Claims claims = mock(Claims.class);
        when(claims.get(Constants.Token.LOGIN_USER_KEY)).thenReturn("uuid");
        when(claims.getId()).thenReturn("jti");
        when(claims.getExpiration())
                .thenReturn(new Date(System.currentTimeMillis() + 60000L));
        when(jwtTokenUtil.parseToken("token")).thenReturn(claims);
        when(redisCacheService.getLoginUserCache()).thenReturn(loginUserCache);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        tokenService.removeToken("token");

        verify(loginUserCache).delete("uuid");
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(keyCaptor.capture(), valueCaptor.capture(), durationCaptor.capture());
        assertEquals("token:blacklist:jti", keyCaptor.getValue());
        assertEquals("1", valueCaptor.getValue());
        assertTrue(durationCaptor.getValue().toMillis() > 0);
    }

    @Test
    void removeToken_ExpiredToken_DoesNotBlacklist() {
        Claims claims = mock(Claims.class);
        when(claims.get(Constants.Token.LOGIN_USER_KEY)).thenReturn("uuid");
        when(claims.getId()).thenReturn("jti");
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() - 1L));
        when(jwtTokenUtil.parseToken("token")).thenReturn(claims);
        when(redisCacheService.getLoginUserCache()).thenReturn(loginUserCache);

        tokenService.removeToken("token");

        verify(loginUserCache).delete("uuid");
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void isTokenBlacklisted_True() {
        when(redisTemplate.hasKey("token:blacklist:jti")).thenReturn(true);
        assertTrue(tokenService.isTokenBlacklisted("jti"));
    }

    @Test
    void isTokenBlacklisted_False() {
        when(redisTemplate.hasKey("token:blacklist:jti")).thenReturn(false);
        assertFalse(tokenService.isTokenBlacklisted("jti"));
    }

    @Test
    void isTokenBlacklisted_NullJti_ReturnsFalse() {
        assertFalse(tokenService.isTokenBlacklisted(null));
    }

    @Test
    void getExpireSeconds_ReturnsConfiguredValue() {
        assertEquals(604800L, tokenService.getExpireSeconds());
    }
}
