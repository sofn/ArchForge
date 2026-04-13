package com.lesofn.appforge.server.admin.service.login;

import com.lesofn.appforge.common.enums.BasicEnumUtil;
import com.lesofn.appforge.common.enums.common.UserStatusEnum;
import com.lesofn.appforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.appforge.infrastructure.config.AppForgeConfig;
import com.lesofn.appforge.infrastructure.user.web.DataScopeEnum;
import com.lesofn.appforge.infrastructure.user.web.RoleInfo;
import com.lesofn.appforge.user.domain.SysMenu;
import com.lesofn.appforge.user.domain.SysRole;
import com.lesofn.appforge.user.domain.SysUser;
import com.lesofn.appforge.user.errors.AdminUserErrorCode;
import com.lesofn.appforge.user.errors.AdminUserException;
import com.lesofn.appforge.user.menu.SysMenuService;
import com.lesofn.appforge.user.service.SysRoleService;
import com.lesofn.appforge.user.service.SysUserService;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 管理员用户详情服务类
 *
 * @author lesofn
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final SysUserService userService;

    private final SysMenuService menuService;

    private final SysRoleService roleService;

    private final TokenService tokenService;

    private final AppForgeConfig appForgeConfig;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userService.getUserByUserName(username);
        if (user == null) {
            log.info("登录用户：{} 不存在.", username);
            throw new AdminUserException(AdminUserErrorCode.USER_NON_EXIST, username);
        }
        if (!Objects.equals(UserStatusEnum.NORMAL.getValue(), user.getStatus())) {
            log.info("登录用户：{} 已被停用.", username);
            throw new AdminUserException(AdminUserErrorCode.USER_IS_DISABLE, username);
        }

        RoleInfo roleInfo = getRoleInfo(user.getRoleId(), user.getIsAdmin());
        SystemLoginUser loginUser =
                new SystemLoginUser(
                        user.getUserId(),
                        user.getIsAdmin(),
                        user.getUsername(),
                        user.getPassword(),
                        roleInfo,
                        user.getDeptId());

        // 填充用户权限信息到authorities中
        if (roleInfo != null && roleInfo.getMenuPermissions() != null) {
            for (String permission : roleInfo.getMenuPermissions()) {
                if (permission != null && !permission.trim().isEmpty()) {
                    loginUser.grantAppPermission(permission);
                }
            }
        }

        // 如果是管理员，添加管理员权限
        if (user.getIsAdmin()) {
            loginUser.grantAppPermission("ROLE_ADMIN");
        }

        // 添加基础用户权限
        loginUser.grantAppPermission("ROLE_USER");

        loginUser.fillLoginInfo();
        loginUser.setAutoRefreshCacheTime(
                loginUser.getLoginInfo().getLoginTime()
                        + TimeUnit.MINUTES.toMillis(
                                appForgeConfig.getToken().getAutoRefreshTime()));
        return loginUser;
    }

    public RoleInfo getRoleInfo(Long roleId, boolean isAdmin) {
        if (roleId == null) {
            return RoleInfo.EMPTY_ROLE;
        }

        if (isAdmin) {
            List<SysMenu> allMenus = menuService.findAllActiveMenus();

            Set<Long> allMenuIds =
                    allMenus.stream().map(SysMenu::getMenuId).collect(Collectors.toSet());

            return new RoleInfo(
                    RoleInfo.ADMIN_ROLE_ID,
                    RoleInfo.ADMIN_ROLE_KEY,
                    DataScopeEnum.ALL,
                    Collections.emptySet(),
                    RoleInfo.ADMIN_PERMISSIONS,
                    allMenuIds);
        }

        SysRole role = roleService.getById(roleId);

        if (role == null) {
            return RoleInfo.EMPTY_ROLE;
        }

        List<SysMenu> menuList = roleService.getMenuListByRoleId(roleId);

        Set<Long> menuIds = menuList.stream().map(SysMenu::getMenuId).collect(Collectors.toSet());
        Set<String> permissions =
                menuList.stream().map(SysMenu::getPermission).collect(Collectors.toSet());

        DataScopeEnum dataScopeEnum =
                BasicEnumUtil.fromValue(DataScopeEnum.class, role.getDataScope());

        Set<Long> deptIdSet = Collections.emptySet();
        if (StringUtils.isNotEmpty(role.getDeptIdSet())) {
            deptIdSet =
                    Arrays.stream(StringUtils.split(role.getDeptIdSet(), ","))
                            .map(NumberUtils::toLong)
                            .collect(Collectors.toSet());
        }

        return new RoleInfo(
                roleId, role.getRoleKey(), dataScopeEnum, deptIdSet, permissions, menuIds);
    }
}
