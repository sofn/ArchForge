package com.lesofn.archsmith.server.admin.util;

import com.lesofn.archsmith.infrastructure.config.ArchSmithConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * JWT工具类
 *
 * @author lesofn
 */
@Component
public class JwtTokenUtil {

    private final ArchSmithConfig appForgeConfig;

    public JwtTokenUtil(ArchSmithConfig appForgeConfig) {
        this.appForgeConfig = appForgeConfig;
    }

    /** 从token中获取用户名 */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /** 从token中获取过期时间 */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /** 从token中获取JWT ID (jti)，用于黑名单识别 */
    public String getJtiFromToken(String token) {
        return getClaimFromToken(token, Claims::getId);
    }

    /** 从token中获取自定义声明 */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /** 解析token获取所有声明 (JJWT 0.12.x API) */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 检查token是否过期 */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /** 生成token */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername());
    }

    /** 创建token (JJWT 0.12.x API)，包含 jti 用于黑名单机制 */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .id(UUID.randomUUID().toString())
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + appForgeConfig.getJwt().getExpireSeconds() * 1000))
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /** 验证token */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /** 获取签名密钥 */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(appForgeConfig.getJwt().getSecret().getBytes());
    }
}
