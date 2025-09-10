package com.lesofn.appboot.server.admin.controller;

import com.lesofn.appboot.infrastructure.auth.AuthenticationUtils;
import com.lesofn.appboot.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.appboot.infrastructure.config.AppBootConfig;
import com.lesofn.appboot.server.admin.dto.*;
import com.lesofn.appboot.server.admin.service.login.LoginService;
import com.lesofn.appboot.server.admin.service.login.TokenService;
import com.lesofn.appboot.server.admin.service.user.UserService;
import com.lesofn.appboot.user.domain.SysMenu;
import com.lesofn.appboot.user.service.SysMenuService;
import com.lesofn.appboot.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    private final SysMenuService menuService;
    private final AppBootConfig appBootConfig;

    /**
     * 访问首页，提示语
     */
    @Operation(summary = "首页")
    @GetMapping("/")
    public String index() {
        return String.format("欢迎使用%s后台管理系统，当前版本：v%s，请通过前端地址访问。", appBootConfig.getName(), appBootConfig.getVersion());
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
     * 登录方法
     *
     * @param loginCommand 登录信息
     * @return 结果
     */
    @Operation(summary = "登录")
    @PostMapping("/login")
    public TokenDTO login(@RequestBody @Valid LoginCommand loginCommand) {
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
        List<SysMenu> menus = menuService.findMenusByRoleId(loginUser.getRoleId());
        List<SysMenu> menuTree = menuService.buildMenuTree(menus);
        return convertToRouterDTO(menuTree);
    }

    /**
     * 将 SysMenu 转换为 RouterDTO
     */
    private List<RouterDTO> convertToRouterDTO(List<SysMenu> sysMenus) {
        if (sysMenus == null || sysMenus.isEmpty()) {
            return new ArrayList<>();
        }

        return sysMenus.stream().map(this::convertMenuToRouter).toList();
    }

    /**
     * 转换单个菜单项
     */
    private RouterDTO convertMenuToRouter(SysMenu menu) {
        RouterDTO router = new RouterDTO();
        router.setName(menu.getRouterName());
        router.setPath(menu.getPath());
        router.setHidden(false); // 根据实际需求设置
        router.setComponent(menu.getMetaInfo()); // 假设 metaInfo 存储了组件路径
        
        RouterDTO.MetaDTO meta = new RouterDTO.MetaDTO();
        meta.setTitle(menu.getMenuName());
        meta.setIcon(""); // 根据实际需求设置图标
        meta.setNoCache(false);
        meta.setLink("");
        router.setMeta(meta);
        
        if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
            router.setChildren(convertToRouterDTO(menu.getChildren()));
        }
        
        return router;
    }

}