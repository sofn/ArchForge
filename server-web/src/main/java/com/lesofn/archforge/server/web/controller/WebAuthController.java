package com.lesofn.archforge.server.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lesofn.archforge.server.web.dto.WebLoginRequest;
import com.lesofn.archforge.server.web.dto.WebLoginResponse;
import com.lesofn.archforge.server.web.dto.WebLogoutRequest;
import com.lesofn.archforge.server.web.dto.WebRefreshTokenRequest;
import com.lesofn.archforge.server.web.service.WebRefreshTokenService;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.api.errors.AdminUserException;
import com.lesofn.archforge.user.api.service.SysUserService;
import com.lesofn.archforge.user.domain.adapter.port.PasswordEncoderPort;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/web")
@RequiredArgsConstructor
public class WebAuthController {

    private static final DateTimeFormatter EXPIRES_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private final SysUserService sysUserService;
    private final PasswordEncoderPort passwordEncoderPort;
    private final WebRefreshTokenService webRefreshTokenService;

    @PostMapping("/login")
    public WebLoginResponse login(@RequestBody @Valid WebLoginRequest request) {
        SysUser user = sysUserService.findByUsername(request.getUsername())
                .orElseThrow(() -> new AdminUserException("用户名或密码错误"));
        if (!user.canLogin()) {
            throw new AdminUserException("用户已被停用");
        }
        if (!passwordEncoderPort.matches(request.getPassword(), user.getPassword())) {
            throw new AdminUserException("用户名或密码错误");
        }

        StpUtil.login(user.getUserId());
        StpUtil.getSession().set("userId", user.getUserId());
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("nickname", user.getNickname());

        String tokenValue = StpUtil.getTokenValue();
        String tokenName = StpUtil.getTokenName();
        long timeout = StpUtil.getTokenTimeout();
        String refreshToken = webRefreshTokenService.createRefreshToken(user.getUserId());

        return buildLoginResponse(user, tokenValue, tokenName, timeout, refreshToken);
    }

    @PostMapping("/refresh-token")
    public WebLoginResponse refreshToken(@RequestBody @Valid WebRefreshTokenRequest request) {
        Long userId = webRefreshTokenService.validateRefreshToken(request.getRefreshToken());
        SysUser user = sysUserService.findById(userId)
                .orElseThrow(() -> new AdminUserException("用户不存在"));
        if (!user.canLogin()) {
            throw new AdminUserException("用户已被停用");
        }

        StpUtil.login(userId);
        StpUtil.getSession().set("userId", userId);
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("nickname", user.getNickname());

        String tokenValue = StpUtil.getTokenValue();
        String tokenName = StpUtil.getTokenName();
        long timeout = StpUtil.getTokenTimeout();
        String refreshToken = webRefreshTokenService.createRefreshToken(userId);

        return buildLoginResponse(user, tokenValue, tokenName, timeout, refreshToken);
    }

    @PostMapping("/logout")
    public Boolean logout(@RequestBody(required = false) WebLogoutRequest request) {
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            webRefreshTokenService.removeRefreshToken(request.getRefreshToken());
        }
        StpUtil.logout();
        return true;
    }

    private WebLoginResponse buildLoginResponse(
            SysUser user, String accessToken, String tokenName, long timeout, String refreshToken) {
        String expires = EXPIRES_FORMATTER.format(
                Instant.now().plusSeconds(timeout)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime());

        return WebLoginResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .accessToken(accessToken)
                .tokenName(tokenName)
                .refreshToken(refreshToken)
                .expires(expires)
                .build();
    }
}
