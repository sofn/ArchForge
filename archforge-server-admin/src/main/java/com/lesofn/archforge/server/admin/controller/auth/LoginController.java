package com.lesofn.archforge.server.admin.controller.auth;

import com.lesofn.archforge.infrastructure.annotation.RateLimit;
import com.lesofn.archforge.infrastructure.auth.LoginContext;
import com.lesofn.archforge.infrastructure.auth.errors.AdminAuthErrorCode;
import com.lesofn.archforge.infrastructure.auth.errors.AdminAuthException;
import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import com.lesofn.archforge.infrastructure.user.web.RoleInfo;
import com.lesofn.archforge.server.admin.dto.*;
import com.lesofn.archforge.server.admin.service.login.LoginService;
import com.lesofn.archforge.server.admin.service.login.TokenService;
import com.lesofn.archforge.server.admin.service.user.AdminUserService;
import com.lesofn.archforge.user.api.menu.SysMenuService;
import com.lesofn.archforge.user.api.menu.dto.RouterDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器 - 登录相关接口
 *
 * @author lesofn
 */
@Slf4j
@Tag(name = "登录API", description = "登录相关接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class LoginController {

    private static final DateTimeFormatter EXPIRES_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private final LoginService loginService;
    private final AdminUserService userService;
    private final SysMenuService menuService;
    private final ArchForgeProperties appForgeConfig;
    private final TokenService tokenService;

    /** 访问首页，提示语 */
    @Operation(summary = "首页")
    @GetMapping("/")
    public String index() {
        return String.format(
                "欢迎使用%s后台管理系统，当前版本：v%s，请通过前端地址访问。",
                appForgeConfig.getName(), appForgeConfig.getVersion());
    }

    /**
     * 获取系统的内置配置
     *
     * @return 配置信息
     */
    @Operation(summary = "获取系统配置")
    @GetMapping("/getConfig")
    public LoginConfigResponse getConfig() { return loginService.getConfig(); }

    /** 生成验证码 */
    @GetMapping("/captchaImage")
    public CaptchaResponse getCaptchaImg() { return loginService.generateCaptchaImg(); }

    /**
     * 登录方法
     *
     * @param loginCommand 登录信息
     * @return 结果
     */
    @Operation(summary = "登录")
    @PostMapping("/login")
    @RateLimit(key = "login", time = 60, maxCount = 5, limitType = RateLimit.LimitType.IP)
    public LoginResponse login(
            @Parameter(description = "登录信息", required = true) @RequestBody @Valid LoginRequest loginCommand) {
        // 生成令牌并获取用户信息
        LoginService.LoginResult loginResult = loginService.login(loginCommand);
        SystemLoginUser loginUser = loginResult.getLoginUser();
        CurrentLoginUserResponse currentUserResponse = userService.getLoginUserInfo(loginUser);

        // 生成刷新令牌
        String refreshToken = tokenService.createRefreshToken(loginUser);

        // 构建前端期望的响应格式
        return buildLoginResponse(loginResult.getToken(), refreshToken, loginUser, currentUserResponse);
    }

    @Operation(summary = "退出登录")
    @SaCheckLogin(type = StpAdminUtil.TYPE)
    @PostMapping("/logout")
    public Boolean logout(@RequestBody(required = false) RefreshTokenRequest command) {
        if (command != null && command.getRefreshToken() != null && !command.getRefreshToken().isBlank()) {
            tokenService.removeRefreshToken(command.getRefreshToken());
        }
        StpAdminUtil.logout();
        return true;
    }

    /**
     * 刷新令牌
     *
     * @param command 刷新令牌请求
     * @return 新的令牌信息
     */
    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh-token")
    public RefreshTokenResponse refreshToken(@RequestBody @Valid RefreshTokenRequest command) {
        SystemLoginUser loginUser = tokenService.getLoginUserByRefreshToken(command.getRefreshToken());
        if (loginUser == null) {
            throw new AdminAuthException(AdminAuthErrorCode.TOKEN_INVALID);
        }

        // 移除旧的刷新令牌
        tokenService.removeRefreshToken(command.getRefreshToken());

        // 生成新的访问令牌和刷新令牌
        String newAccessToken = tokenService.createTokenAndPutUserInCache(loginUser);
        String newRefreshToken = tokenService.createRefreshToken(loginUser);

        RefreshTokenResponse responseDTO = new RefreshTokenResponse();
        responseDTO.setAccessToken(newAccessToken);
        responseDTO.setRefreshToken(newRefreshToken);
        responseDTO.setExpires(calculateExpires());
        return responseDTO;
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @Operation(summary = "获取当前登录用户信息")
    @SaCheckLogin(type = StpAdminUtil.TYPE)
    @GetMapping("/getLoginUserInfo")
    public CurrentLoginUserResponse getLoginUserInfo() {
        SystemLoginUser loginUser = LoginContext.getAdminUser();
        return userService.getLoginUserInfo(loginUser);
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @Operation(summary = "获取用户对应的菜单路由", description = "用于动态生成路由")
    @SaCheckLogin(type = StpAdminUtil.TYPE)
    @GetMapping("/getRouters")
    public List<RouterDTO> getRouters() {
        SystemLoginUser loginUser = LoginContext.getAdminUser();
        return menuService.getRouterTree(loginUser);
    }

    /**
     * 获取异步路由信息（兼容vue-pure-admin前端）
     *
     * @return 路由信息
     */
    @Operation(summary = "获取异步路由", description = "兼容vue-pure-admin前端的路由获取接口")
    @SaCheckLogin(type = StpAdminUtil.TYPE)
    @GetMapping("/get-async-routes")
    public List<RouterDTO> getAsyncRoutes() {
        SystemLoginUser loginUser = LoginContext.getAdminUser();
        return menuService.getRouterTree(loginUser);
    }

    /** 构建登录响应DTO */
    private LoginResponse buildLoginResponse(
            String accessToken,
            String refreshToken,
            SystemLoginUser loginUser,
            CurrentLoginUserResponse currentUserResponse) {
        LoginResponse responseDTO = new LoginResponse();
        responseDTO.setAccessToken(accessToken);
        responseDTO.setRefreshToken(refreshToken);
        responseDTO.setExpires(calculateExpires());

        // 用户基本信息
        UserResponse userInfo = currentUserResponse.getUserInfo();
        if (userInfo != null) {
            responseDTO.setAvatar(userInfo.getAvatar());
            responseDTO.setUsername(userInfo.getUsername());
            responseDTO.setNickname(userInfo.getNickname());
        } else {
            responseDTO.setUsername(loginUser.getUsername());
        }

        // 角色信息
        List<String> roles = new ArrayList<>();
        if (loginUser.isAdmin()) {
            roles.add(RoleInfo.ADMIN_ROLE_KEY);
        } else {
            RoleInfo roleInfo = loginUser.getRoleInfo();
            if (roleInfo != null && roleInfo.getRoleKey() != null) {
                roles.add(roleInfo.getRoleKey());
            }
        }
        responseDTO.setRoles(roles);

        // 权限信息
        if (loginUser.isAdmin()) {
            responseDTO.setPermissions(Collections.singletonList(RoleInfo.ALL_PERMISSIONS));
        } else {
            Set<String> permissionSet = currentUserResponse.getPermissions();
            if (permissionSet != null) {
                responseDTO.setPermissions(new ArrayList<>(permissionSet));
            } else {
                responseDTO.setPermissions(Collections.emptyList());
            }
        }

        return responseDTO;
    }

    /** 计算过期时间字符串 */
    private String calculateExpires() {
        long expireSeconds = StpAdminUtil.getTokenTimeout();
        if (expireSeconds <= 0) {
            expireSeconds = tokenService.getExpireSeconds();
        }
        Instant expireInstant = Instant.now().plusSeconds(expireSeconds);
        return EXPIRES_FORMATTER.format(
                expireInstant.atZone(ZoneId.systemDefault()).toLocalDateTime());
    }
}
