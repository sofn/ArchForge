package com.lesofn.archforge.server.web.controller;

import com.lesofn.archforge.infrastructure.auth.stp.LoginSessionKeys;
import com.lesofn.archforge.infrastructure.auth.stp.StpWebUtil;
import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import com.lesofn.archforge.server.web.dto.WebLoginRequest;
import com.lesofn.archforge.server.web.dto.WebLoginResponse;
import com.lesofn.archforge.server.web.dto.WebLogoutRequest;
import com.lesofn.archforge.server.web.dto.WebRefreshTokenRequest;
import com.lesofn.archforge.server.web.dto.WebRegisterRequest;
import com.lesofn.archforge.server.web.dto.WebResetPasswordRequest;
import com.lesofn.archforge.server.web.dto.WebSendVerificationCodeRequest;
import com.lesofn.archforge.server.web.errors.WebAuthErrorCode;
import com.lesofn.archforge.server.web.errors.WebAuthException;
import com.lesofn.archforge.server.web.service.VerificationCodePurpose;
import com.lesofn.archforge.server.web.service.VerificationCodeService;
import com.lesofn.archforge.server.web.service.WebRefreshTokenService;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.api.errors.AdminUserException;
import com.lesofn.archforge.user.api.service.SysUserService;
import com.lesofn.archforge.user.domain.adapter.port.PasswordEncoderPort;
import jakarta.validation.Valid;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
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
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");
    private static final int USERNAME_MAX_LENGTH = 64;
    private static final int NICKNAME_MAX_LENGTH = 32;
    private static final int MAX_USERNAME_GENERATION_ATTEMPTS = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SysUserService sysUserService;
    private final PasswordEncoderPort passwordEncoderPort;
    private final WebRefreshTokenService webRefreshTokenService;
    private final VerificationCodeService verificationCodeService;
    private final ArchForgeProperties archForgeConfig;

    @PostMapping("/login")
    public WebLoginResponse login(@RequestBody @Valid WebLoginRequest request) {
        SysUser user = sysUserService.findByUsername(request.getUsername())
                .or(() -> sysUserService.findByEmail(request.getUsername()))
                .orElseThrow(() -> new AdminUserException("用户名或密码错误"));
        if (!user.canLogin()) {
            throw new AdminUserException("用户已被停用");
        }
        if (!passwordEncoderPort.matches(request.getPassword(), user.getPassword())) {
            throw new AdminUserException("用户名或密码错误");
        }

        writeLoginSession(user);

        String tokenValue = StpWebUtil.getTokenValue();
        String tokenName = StpWebUtil.getTokenName();
        long timeout = StpWebUtil.getTokenTimeout();
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

        writeLoginSession(user);

        String tokenValue = StpWebUtil.getTokenValue();
        String tokenName = StpWebUtil.getTokenName();
        long timeout = StpWebUtil.getTokenTimeout();
        String refreshToken = webRefreshTokenService.createRefreshToken(userId);

        return buildLoginResponse(user, tokenValue, tokenName, timeout, refreshToken);
    }

    @PostMapping("/logout")
    public Boolean logout(@RequestBody(required = false) WebLogoutRequest request) {
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            webRefreshTokenService.removeRefreshToken(request.getRefreshToken());
        }
        StpWebUtil.logout();
        return true;
    }

    @PostMapping("/verification-code/send")
    public Boolean sendVerificationCode(@RequestBody @Valid WebSendVerificationCodeRequest request) {
        VerificationCodePurpose purpose = parsePurpose(request.getPurpose());
        if (purpose == VerificationCodePurpose.REGISTER && sysUserService.existsByEmail(request.getEmail())) {
            throw new WebAuthException(WebAuthErrorCode.EMAIL_ALREADY_REGISTERED);
        }
        if (purpose == VerificationCodePurpose.RESET_PASSWORD && !sysUserService.existsByEmail(request.getEmail())) {
            throw new WebAuthException(WebAuthErrorCode.EMAIL_NOT_REGISTERED);
        }
        return verificationCodeService.send(request.getEmail(), purpose);
    }

    @PostMapping("/register")
    public WebLoginResponse register(@RequestBody @Valid WebRegisterRequest request) {
        if (!archForgeConfig.getRegister().isEnabled()) {
            throw new WebAuthException(WebAuthErrorCode.REGISTER_DISABLED);
        }
        validatePassword(request.getPassword(), request.getConfirmPassword());
        verificationCodeService.verify(request.getEmail(), request.getCode(), VerificationCodePurpose.REGISTER);

        if (sysUserService.existsByEmail(request.getEmail())) {
            throw new WebAuthException(WebAuthErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        SysUser user = createUserFromRequest(request);
        SysUser savedUser = sysUserService.create(user);

        writeLoginSession(savedUser);

        String tokenValue = StpWebUtil.getTokenValue();
        String tokenName = StpWebUtil.getTokenName();
        long timeout = StpWebUtil.getTokenTimeout();
        String refreshToken = webRefreshTokenService.createRefreshToken(savedUser.getUserId());

        return buildLoginResponse(savedUser, tokenValue, tokenName, timeout, refreshToken);
    }

    @PostMapping("/forgot-password")
    public Boolean forgotPassword(@RequestBody @Valid WebSendVerificationCodeRequest request) {
        if (!sysUserService.existsByEmail(request.getEmail())) {
            throw new WebAuthException(WebAuthErrorCode.EMAIL_NOT_REGISTERED);
        }
        return verificationCodeService.send(request.getEmail(), VerificationCodePurpose.RESET_PASSWORD);
    }

    @PostMapping("/reset-password")
    public Boolean resetPassword(@RequestBody @Valid WebResetPasswordRequest request) {
        validatePassword(request.getNewPassword(), request.getConfirmPassword());
        verificationCodeService.verify(request.getEmail(), request.getCode(), VerificationCodePurpose.RESET_PASSWORD);

        SysUser user = sysUserService.findByEmail(request.getEmail())
                .orElseThrow(() -> new WebAuthException(WebAuthErrorCode.EMAIL_NOT_REGISTERED));
        String encodedPassword = passwordEncoderPort.encode(request.getNewPassword());
        sysUserService.updatePassword(user.getUserId(), encodedPassword);
        return true;
    }

    private VerificationCodePurpose parsePurpose(String purpose) {
        if (purpose == null || purpose.isBlank()) {
            throw new WebAuthException("验证码用途错误");
        }
        try {
            return VerificationCodePurpose.valueOf(purpose.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new WebAuthException("验证码用途错误");
        }
    }

    private void validatePassword(String password, String confirmPassword) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new WebAuthException(WebAuthErrorCode.PASSWORD_WEAK);
        }
        if (!password.equals(confirmPassword)) {
            throw new WebAuthException(WebAuthErrorCode.PASSWORD_MISMATCH);
        }
    }

    private SysUser createUserFromRequest(WebRegisterRequest request) {
        String username = generateUsername(request.getEmail());
        String nickname = username.length() > NICKNAME_MAX_LENGTH ? username.substring(0, NICKNAME_MAX_LENGTH) : username;

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setNickname(nickname);
        user.setEmail(request.getEmail());
        user.prepareForCreate(passwordEncoderPort.encode(request.getPassword()));
        return user;
    }

    private String generateUsername(String email) {
        int atIndex = email.indexOf('@');
        String base = atIndex > 0 ? email.substring(0, atIndex) : email;
        if (base.length() > USERNAME_MAX_LENGTH) {
            base = base.substring(0, USERNAME_MAX_LENGTH);
        }

        String username = base;
        for (int attempt = 0; attempt < MAX_USERNAME_GENERATION_ATTEMPTS; attempt++) {
            if (!sysUserService.existsByUsername(username)) {
                return username;
            }
            String suffix = String.valueOf(1000 + SECURE_RANDOM.nextInt(9000));
            int maxBaseLength = USERNAME_MAX_LENGTH - suffix.length();
            String adjustedBase = base.length() > maxBaseLength ? base.substring(0, maxBaseLength) : base;
            username = adjustedBase + suffix;
        }
        throw new WebAuthException("无法生成唯一用户名，请稍后重试");
    }

    private void writeLoginSession(SysUser user) {
        StpWebUtil.login(user.getUserId());
        StpWebUtil.getSession().set(LoginSessionKeys.USERNAME, user.getUsername());
        StpWebUtil.getSession().set("nickname", user.getNickname());
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
