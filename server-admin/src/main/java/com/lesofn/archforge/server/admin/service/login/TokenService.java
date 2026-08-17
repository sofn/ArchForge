package com.lesofn.archforge.server.admin.service.login;

import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.auth.stp.LoginSessionKeys;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import com.lesofn.archforge.server.admin.service.cache.RedisCacheService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TokenService {

    private final ArchForgeProperties appForgeConfig;
    private final RedisCacheService redisCacheService;
    private final AdminLoginUserFactory adminLoginUserFactory;

    public String createTokenAndPutUserInCache(SystemLoginUser loginUser) {
        StpAdminUtil.login(loginUser.getUserId());
        StpAdminUtil.getSession().set(LoginSessionKeys.LOGIN_USER, loginUser);
        StpAdminUtil.getSession().set(LoginSessionKeys.USERNAME, loginUser.getUsername());
        return StpAdminUtil.getTokenValue();
    }

    public void refreshToken(SystemLoginUser loginUser) {
        // sa-token session already holds the latest login user
    }

    public String createRefreshToken(SystemLoginUser loginUser) {
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        redisCacheService.getRefreshTokenCache().set(refreshToken, String.valueOf(loginUser.getUserId()));
        return refreshToken;
    }

    public SystemLoginUser getLoginUserByRefreshToken(String refreshToken) {
        String userId = redisCacheService.getRefreshTokenCache().get(refreshToken);
        if (userId == null) {
            return null;
        }
        try {
            return adminLoginUserFactory.loadByUserId(Long.parseLong(userId));
        } catch (Exception e) {
            log.warn("Failed to reload login user for refresh token, userId={}", userId, e);
            return null;
        }
    }

    public void removeRefreshToken(String refreshToken) {
        redisCacheService.getRefreshTokenCache().delete(refreshToken);
    }

    public long getExpireSeconds() {
        long timeout = StpAdminUtil.getTokenTimeout();
        if (timeout > 0) {
            return timeout;
        }
        return 604800L;
    }

    public void removeToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        StpAdminUtil.STP_LOGIC.logoutByTokenValue(token);
    }
}
