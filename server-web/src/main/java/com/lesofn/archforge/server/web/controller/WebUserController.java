package com.lesofn.archforge.server.web.controller;

import com.lesofn.archforge.server.web.context.WebUserContext;
import com.lesofn.archforge.server.web.dto.WebChangePasswordRequest;
import com.lesofn.archforge.server.web.dto.WebUserProfileResponse;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.api.service.SysUserService;
import com.lesofn.archforge.user.domain.adapter.port.PasswordEncoderPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/web/user")
@RequiredArgsConstructor
public class WebUserController {

    private final SysUserService sysUserService;
    private final PasswordEncoderPort passwordEncoderPort;

    @GetMapping("/profile")
    public WebUserProfileResponse profile() {
        SysUser user = sysUserService.findById(WebUserContext.getUserId())
                .orElseThrow(() -> new com.lesofn.archforge.user.api.errors.AdminUserException("用户不存在"));
        return WebUserProfileResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    @PostMapping("/change-password")
    public Boolean changePassword(@RequestBody @Valid WebChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new com.lesofn.archforge.user.api.errors.AdminUserException("两次输入的新密码不一致");
        }
        SysUser user = sysUserService.findById(WebUserContext.getUserId())
                .orElseThrow(() -> new com.lesofn.archforge.user.api.errors.AdminUserException("用户不存在"));
        if (!passwordEncoderPort.matches(request.getOldPassword(), user.getPassword())) {
            throw new com.lesofn.archforge.user.api.errors.AdminUserException("旧密码错误");
        }
        String encoded = passwordEncoderPort.encode(request.getNewPassword());
        sysUserService.updatePassword(user.getUserId(), encoded);
        return true;
    }
}
