package com.lesofn.archforge.server.web.service;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.dao.SaTokenDao;
import com.lesofn.archforge.user.api.errors.AdminUserException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class WebRefreshTokenService {

    private static final String REFRESH_TOKEN_KEY_PREFIX = "web-refresh-token:";

    public String createRefreshToken(Long userId) {
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        SaTokenDao tokenDao = SaManager.getSaTokenDao();
        long timeout = SaManager.getConfig().getTimeout();
        tokenDao.set(REFRESH_TOKEN_KEY_PREFIX + refreshToken, String.valueOf(userId), timeout);
        return refreshToken;
    }

    public Long validateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AdminUserException("refresh token 无效或已过期");
        }
        SaTokenDao tokenDao = SaManager.getSaTokenDao();
        String key = REFRESH_TOKEN_KEY_PREFIX + refreshToken;
        String userIdValue = tokenDao.get(key);
        if (userIdValue == null || userIdValue.isBlank()) {
            throw new AdminUserException("refresh token 无效或已过期");
        }
        try {
            long userId = Long.parseLong(userIdValue);
            tokenDao.delete(key);
            return userId;
        } catch (NumberFormatException e) {
            tokenDao.delete(key);
            throw new AdminUserException("refresh token 无效或已过期");
        }
    }

    public void removeRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        SaManager.getSaTokenDao().delete(REFRESH_TOKEN_KEY_PREFIX + refreshToken);
    }
}
