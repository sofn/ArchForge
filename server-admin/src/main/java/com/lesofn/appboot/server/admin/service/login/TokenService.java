package com.lesofn.appboot.server.admin.service.login;


import com.google.common.collect.ImmutableMap;
import com.lesofn.appboot.common.constant.Constants;
import com.lesofn.appboot.common.exception.ApiException;
import com.lesofn.appboot.common.exception.ErrorCode;
import com.lesofn.appboot.server.admin.service.cache.RedisCacheService;
import com.lesofn.appboot.infrastructure.auth.model.SystemLoginUser;
import io.jsonwebtoken.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    /**
     * 自定义令牌标识
     */
    @Value("${token.header}")
    private String header;

    /**
     * 令牌秘钥
     */
    @Value("${secret.secret}")
    private String secret;

    /**
     * 自动刷新token的时间，当过期时间不足autoRefreshTime的值的时候，会触发刷新用户登录缓存的时间
     * 比如这个值是20,   用户是8点登录的， 8点半缓存会过期， 当过8.10分的时候，就少于20分钟了，便触发
     * 刷新登录用户的缓存时间
     */
    @Value("${token.autoRefreshTime}")
    private long autoRefreshTime;

    private final RedisCacheService redisCacheService;


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

                return redisCacheService.loginUserCache.getObjectById(uuid);
            } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException jwtException) {
                log.error("parse token failed.", jwtException);
                throw new ApiException(jwtException, ErrorCode.Client.INVALID_TOKEN);
            } catch (Exception e) {
                log.error("fail to get cached user from redis", e);
                throw new ApiException(e, ErrorCode.Client.TOKEN_PROCESS_FAILED, e.getMessage());
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

        return generateToken(ImmutableMap.of(Constants.Token.LOGIN_USER_KEY, loginUser.getCachedKey()));
    }

    /**
     * 当超过20分钟，自动刷新token
     * @param loginUser 登录用户
     */
    public void refreshToken(SystemLoginUser loginUser) {
        long currentTime = System.currentTimeMillis();
        if (currentTime > loginUser.getAutoRefreshCacheTime()) {
            loginUser.setAutoRefreshCacheTime(currentTime + TimeUnit.MINUTES.toMillis(autoRefreshTime));
            // 根据uuid将loginUser存入缓存
            redisCacheService.loginUserCache.set(loginUser.getCachedKey(), loginUser);
        }
    }


    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
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
        String token = request.getHeader(header);
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.Token.PREFIX)) {
            token = StringUtils.strip(token, Constants.Token.PREFIX);
        }
        return token;
    }

}
