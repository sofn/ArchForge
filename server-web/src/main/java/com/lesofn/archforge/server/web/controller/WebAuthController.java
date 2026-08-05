package com.lesofn.archforge.server.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lesofn.archforge.server.web.dto.WebLoginRequest;
import com.lesofn.archforge.server.web.dto.WebLoginResponse;
import com.lesofn.archforge.user.api.domain.SysUser;
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

    @PostMapping("/login")
    public WebLoginResponse login(@RequestBody @Valid WebLoginRequest request) {
        SysUser user = sysUserService.findByUsername(request.getUsername())
                .orElseThrow(() -> new com.lesofn.archforge.user.api.errors.AdminUserException("用户名或密码错误"));
        if (!user.canLogin()) {
            throw new com.lesofn.archforge.user.api.errors.AdminUserException("用户已被停用");
        }
        if (!passwordEncoderPort.matches(request.getPassword(), user.getPassword())) {
            throw new com.lesofn.archforge.user.api.errors.AdminUserException("用户名或密码错误");
        }

        StpUtil.login(user.getUserId());
        StpUtil.getSession().set("userId", user.getUserId());
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("nickname", user.getNickname());

        String tokenValue = StpUtil.getTokenValue();
        String tokenName = StpUtil.getTokenName();
        long timeout = StpUtil.getTokenTimeout();
        String expires = EXPIRES_FORMATTER.format(
                Instant.now().plusSeconds(timeout)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime());

        return WebLoginResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .accessToken(tokenValue)
                .tokenName(tokenName)
                .expires(expires)
                .build();
    }

    @PostMapping("/logout")
    public Boolean logout() {
        StpUtil.logout();
        return true;
    }
}
