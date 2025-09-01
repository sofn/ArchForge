package com.lesofn.appboot.server.admin.service.login;

import com.lesofn.appboot.common.enums.BasicEnumUtil;
import com.lesofn.appboot.common.enums.common.UserStatusEnum;
import com.lesofn.appboot.common.errors.UserErrorCode;
import com.lesofn.appboot.common.errors.UserException;
import com.lesofn.appboot.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.appboot.infrastructure.user.web.DataScopeEnum;
import com.lesofn.appboot.infrastructure.user.web.RoleInfo;
import com.lesofn.appboot.user.domain.SysMenu;
import com.lesofn.appboot.user.domain.SysRole;
import com.lesofn.appboot.user.domain.SysUser;
import com.lesofn.appboot.user.service.SysMenuService;
import com.lesofn.appboot.user.service.SysRoleService;
import com.lesofn.appboot.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userService.getUserByUserName(username);
        if (user == null) {
            log.info("登录用户：{} 不存在.", username);
            throw new UserException(UserErrorCode.USER_NON_EXIST, username);
        }
        if (!Objects.equals(UserStatusEnum.NORMAL.getValue(), user.getStatus())) {
            log.info("登录用户：{} 已被停用.", username);
            throw new UserException(UserErrorCode.USER_IS_DISABLE, username);
        }

        RoleInfo roleInfo = getRoleInfo(user.getRoleId(), user.getIsAdmin());

        SystemLoginUser loginUser = new SystemLoginUser(user.getUserId(), user.getIsAdmin(), user.getUsername(),
                user.getPassword(), roleInfo, user.getDeptId());
        loginUser.fillLoginInfo();
        loginUser.setAutoRefreshCacheTime(loginUser.getLoginInfo().getLoginTime()
                + TimeUnit.MINUTES.toMillis(tokenService.getAutoRefreshTime()));
        return loginUser;
    }

    public RoleInfo getRoleInfo(Long roleId, boolean isAdmin) {
        if (roleId == null) {
            return RoleInfo.EMPTY_ROLE;
        }

        if (isAdmin) {
            List<SysMenu> allMenus = menuService.findAllActiveMenus();

            Set<Long> allMenuIds = allMenus.stream().map(SysMenu::getMenuId).collect(Collectors.toSet());

            return new RoleInfo(RoleInfo.ADMIN_ROLE_ID, RoleInfo.ADMIN_ROLE_KEY, DataScopeEnum.ALL, Collections.emptySet(),
                    RoleInfo.ADMIN_PERMISSIONS, allMenuIds);

        }

        SysRole role = roleService.getById(roleId);

        if (role == null) {
            return RoleInfo.EMPTY_ROLE;
        }

        List<SysMenu> menuList = roleService.getMenuListByRoleId(roleId);

        Set<Long> menuIds = menuList.stream().map(SysMenu::getMenuId).collect(Collectors.toSet());
        Set<String> permissions = menuList.stream().map(SysMenu::getPermission).collect(Collectors.toSet());

        DataScopeEnum dataScopeEnum = BasicEnumUtil.fromValue(DataScopeEnum.class, role.getDataScope());

        Set<Long> deptIdSet = Collections.emptySet();
        if (StringUtils.isNotEmpty(role.getDeptIdSet())) {
            deptIdSet = Arrays.stream(StringUtils.split(role.getDeptIdSet(), ","))
                    .map(NumberUtils::toLong)
                    .collect(Collectors.toSet());
        }

        return new RoleInfo(roleId, role.getRoleKey(), dataScopeEnum, deptIdSet, permissions, menuIds);
    }

}