package com.lesofn.archforge.server.web.util;

import com.lesofn.archforge.infrastructure.config.ArchForgeConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class WebJwtTokenUtil {

    private final ArchForgeConfig appForgeConfig;

    public WebJwtTokenUtil(ArchForgeConfig appForgeConfig) {
        this.appForgeConfig = appForgeConfig;
    }

    public long getExpireSeconds() { return appForgeConfig.getJwt().getExpireSeconds(); }

    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return doGenerateToken(claims, username);
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        Object userId = claims.get("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    private boolean isTokenExpired(String token) {
        Date expiration = parseToken(token).getExpiration();
        return expiration.before(new Date());
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .id(UUID.randomUUID().toString())
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + getExpireSeconds() * 1000))
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    private SecretKey getSigningKey() { return Keys.hmacShaKeyFor(appForgeConfig.getJwt().getSecret().getBytes()); }
}
