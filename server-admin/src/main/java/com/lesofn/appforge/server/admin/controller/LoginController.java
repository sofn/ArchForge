package com.lesofn.appforge.server.admin.controller;

import com.lesofn.appforge.common.utils.jackson.JsonUtil;
import com.lesofn.appforge.infrastructure.auth.AuthenticationUtils;
import com.lesofn.appforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.appforge.infrastructure.config.AppForgeConfig;
import com.lesofn.appforge.server.admin.dto.*;
import com.lesofn.appforge.server.admin.service.login.LoginService;
import com.lesofn.appforge.server.admin.service.user.UserService;
import com.lesofn.appforge.user.menu.SysMenuService;
import com.lesofn.appforge.user.menu.dto.RouterDTO;
import com.lesofn.appforge.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器 - 登录相关接口
 *
 * @author lesofn
 */
@Slf4j
@Tag(name = "登录API", description = "登录相关接口")
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final UserService userService;
    private final SysUserService sysUserService;
    private final SysMenuService menuService;
    private final AppForgeConfig appForgeConfig;

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
    public ConfigDTO getConfig() {
        log.info("user: {}", JsonUtil.to(sysUserService.getUserByUserName("admin")));
        log.info("user: {}", JsonUtil.to(sysUserService.getUserByUserName("ag1")));
        return loginService.getConfig();
    }

    /** 生成验证码 */
    @GetMapping("/captchaImage")
    public CaptchaDTO getCaptchaImg() {
        return loginService.generateCaptchaImg();
    }

    /**
     * 登录方法
     *
     * @param loginCommand 登录信息
     * @return 结果
     */
    @Operation(summary = "登录")
    @PostMapping("/login")
    public TokenDTO login(
            @Parameter(description = "登录信息", required = true) @RequestBody @Valid
                    LoginCommand loginCommand) {
        // 生成令牌并获取用户信息
        LoginService.LoginResult loginResult = loginService.login(loginCommand);
        SystemLoginUser loginUser = loginResult.getLoginUser();
        CurrentLoginUserDTO currentUserDTO = userService.getLoginUserInfo(loginUser);

        return new TokenDTO(loginResult.getToken(), currentUserDTO);
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/getLoginUserInfo")
    public CurrentLoginUserDTO getLoginUserInfo() {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        return userService.getLoginUserInfo(loginUser);
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @Operation(summary = "获取用户对应的菜单路由", description = "用于动态生成路由")
    @GetMapping("/getRouters")
    public List<RouterDTO> getRouters() {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        return menuService.getRouterTree(loginUser);
    }
}
