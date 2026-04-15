package com.lesofn.archsmith.server.admin.service.login;

import static com.lesofn.archsmith.infrastructure.auth.errors.AdminAuthErrorCode.TOKEN_INVALID;

import com.lesofn.archsmith.common.constant.Constants;
import com.lesofn.archsmith.infrastructure.auth.errors.AdminAuthException;
import com.lesofn.archsmith.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archsmith.infrastructure.config.ArchSmithConfig;
import com.lesofn.archsmith.server.admin.service.cache.RedisCacheService;
import com.lesofn.archsmith.server.admin.util.JwtTokenUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * token验证处理
 *
 * @author sofn
 */
@Component
@Slf4j
@Data
@RequiredArgsConstructor
public class TokenService {

    private final ArchSmithConfig appForgeConfig;
    private final RedisCacheService redisCacheService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public SystemLoginUser getLoginUser(HttpServletRequest request) {
        // 获取请求携带的令牌
        String token = getTokenFromRequest(request);
        if (StringUtils.isNotEmpty(token)) {
            try {
                Claims claims = parseToken(token);
                // 解析对应的权限以及用户信息
                String uuid = (String) claims.get(Constants.Token.LOGIN_USER_KEY);

                return redisCacheService.loginUserCache.get(uuid);
            } catch (MalformedJwtException
                    | UnsupportedJwtException
                    | IllegalArgumentException jwtException) {
                log.error("parse token failed.", jwtException);
                throw new AdminAuthException(TOKEN_INVALID);
            } catch (Exception e) {
                log.error("fail to get cached user from redis", e);
                throw new AdminAuthException(TOKEN_INVALID);
            }
        }
        return null;
    }

    /**
     * 创建令牌
     *
     * @param loginUser 用户信息
     * @return 令牌
     */
    public String createTokenAndPutUserInCache(SystemLoginUser loginUser) {
        loginUser.setCachedKey(UUID.randomUUID().toString().replace("-", ""));
        redisCacheService.loginUserCache.set(loginUser.getCachedKey(), loginUser);
        return jwtTokenUtil.generateToken(loginUser);
    }

    /**
     * 当超过20分钟，自动刷新token
     *
     * @param loginUser 登录用户
     */
    public void refreshToken(SystemLoginUser loginUser) {
        long currentTime = System.currentTimeMillis();
        if (currentTime > loginUser.getAutoRefreshCacheTime()) {
            loginUser.setAutoRefreshCacheTime(
                    currentTime
                            + TimeUnit.MINUTES.toMillis(
                                    appForgeConfig.getToken().getAutoRefreshTime()));
            // 根据uuid将loginUser存入缓存
            redisCacheService.loginUserCache.set(loginUser.getCachedKey(), loginUser);
        }
    }

    /**
     * 创建刷新令牌
     *
     * @param loginUser 登录用户
     * @return 刷新令牌
     */
    public String createRefreshToken(SystemLoginUser loginUser) {
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        // 存储 refreshToken -> cachedKey 的映射
        redisCacheService.refreshTokenCache.set(refreshToken, loginUser.getCachedKey());
        return refreshToken;
    }

    /**
     * 根据刷新令牌获取登录用户
     *
     * @param refreshToken 刷新令牌
     * @return 登录用户
     */
    public SystemLoginUser getLoginUserByRefreshToken(String refreshToken) {
        String cachedKey = redisCacheService.refreshTokenCache.get(refreshToken);
        if (cachedKey == null) {
            return null;
        }
        return redisCacheService.loginUserCache.get(cachedKey);
    }

    /**
     * 移除刷新令牌
     *
     * @param refreshToken 刷新令牌
     */
    public void removeRefreshToken(String refreshToken) {
        redisCacheService.refreshTokenCache.delete(refreshToken);
    }

    /**
     * 获取JWT过期时间（秒）
     *
     * @return 过期时间秒数
     */
    public long getExpireSeconds() {
        return appForgeConfig.getJwt().getExpireSeconds();
    }

    /**
     * 删除用户身份信息
     *
     * @param token 令牌
     */
    public void removeToken(String token) {
        if (StringUtils.isNotEmpty(token)) {
            try {
                Claims claims = parseToken(token);
                // 解析对应的权限以及用户信息
                String uuid = (String) claims.get(Constants.Token.LOGIN_USER_KEY);
                // 删除用户缓存记录
                redisCacheService.loginUserCache.delete(uuid);
            } catch (Exception e) {
                log.warn("Failed to remove token from cache", e);
            }
        }
    }

    /**
     * 从令牌中获取数据声明 (JJWT 0.12.x API)
     *
     * @param token 令牌
     * @return 数据声明
     */
    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(appForgeConfig.getJwt().getSecret().getBytes());

        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    private String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 获取请求token
     *
     * @return token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(appForgeConfig.getToken().getHeader());
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.Token.PREFIX)) {
            token = StringUtils.strip(token, Constants.Token.PREFIX);
        }
        return token;
    }
}
