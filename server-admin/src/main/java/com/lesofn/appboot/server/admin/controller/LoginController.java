package com.lesofn.appboot.server.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lesofn.appboot.common.errors.EngineExceptionHelper;
import com.lesofn.appboot.server.admin.dto.*;
import com.lesofn.appboot.server.admin.service.login.LoginService;
import com.lesofn.appboot.server.admin.service.login.TokenService;
import com.lesofn.appboot.server.admin.service.user.UserService;
import com.lesofn.appboot.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.appboot.infrastructure.auth.spi.MAuthSpi;
import com.lesofn.appboot.user.domain.SysUser;
import com.lesofn.appboot.user.service.SysUserService;
import com.lesofn.appboot.user.utils.UserExcepFactor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证控制器 - 登录相关接口
 *
 * @author lesofn
 */
@Tag(name = "登录API", description = "登录相关接口")
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final TokenService tokenService;
    private final UserService userService;
    private final SysUserService sysUserService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.application.name:AppBoot}")
    private String applicationName;

    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    /**
     * 访问首页，提示语
     */
    @Operation(summary = "首页")
    @GetMapping("/")
    public String index() {
        return String.format("欢迎使用%s后台管理框架，当前版本：v%s，请通过前端地址访问。", applicationName, applicationVersion);
    }

    /**
     * 获取系统的内置配置
     *
     * @return 配置信息
     */
    @Operation(summary = "获取系统配置")
    @GetMapping("/getConfig")
    public ConfigDTO getConfig() {
        return loginService.getConfig();
    }

    /**
     * 生成验证码
     */
    @GetMapping("/captchaImage")
    public CaptchaDTO getCaptchaImg() {
        return loginService.generateCaptchaImg();
    }

    /**
     * 登录方法 - 原始登录接口（兼容旧接口）
     *
     * @param username 用户名
     * @param password 密码
     * @return 结果
     */
    @Operation(summary = "登录（兼容旧接口）")
    @PostMapping("/api/login")
    public ObjectNode add(@RequestParam String username, @RequestParam String password) {
        ObjectNode result = objectMapper.createObjectNode();
        SysUser user = sysUserService.findByUsername(username)
                .orElseThrow(() -> EngineExceptionHelper.localException(UserExcepFactor.USERPASS_ERROR));
        // 这里简化处理，实际应该验证密码
        result.set("user", objectMapper.valueToTree(user));
        result.put("mauth", MAuthSpi.generateMauth(user.getUserId()));
        return result;
    }

    /**
     * 登录方法
     *
     * @param loginCommand 登录信息
     * @return 结果
     */
    @Operation(summary = "登录")
    @PostMapping("/login")
    public TokenDTO login(@RequestBody @Valid LoginCommand loginCommand) {
        // 生成令牌
        String token = loginService.login(loginCommand);
        SystemLoginUser loginUser = getLoginUser();
        CurrentLoginUserDTO currentUserDTO = userService.getLoginUserInfo(loginUser);

        return new TokenDTO(token, currentUserDTO);
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/getLoginUserInfo")
    public CurrentLoginUserDTO getLoginUserInfo() {
        SystemLoginUser loginUser = getLoginUser();
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
        SystemLoginUser loginUser = getLoginUser();
        // 暂时返回空列表，因为MenuService不存在
        return new ArrayList<>();
    }

    /**
     * 获取当前登录用户
     */
    private SystemLoginUser getLoginUser() {
        // TODO: 从 SecurityContext 或 Token 中获取当前登录用户
        return new SystemLoginUser();
    }
}