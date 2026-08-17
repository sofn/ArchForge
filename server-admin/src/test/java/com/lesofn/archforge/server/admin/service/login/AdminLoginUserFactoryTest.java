package com.lesofn.archforge.server.admin.service.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.common.enums.common.UserStatusEnum;
import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import com.lesofn.archforge.infrastructure.user.web.DataScopeEnum;
import com.lesofn.archforge.infrastructure.user.web.RoleInfo;
import com.lesofn.archforge.user.api.domain.SysMenu;
import com.lesofn.archforge.user.api.domain.SysRole;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.api.errors.AdminUserErrorCode;
import com.lesofn.archforge.user.api.errors.AdminUserException;
import com.lesofn.archforge.user.api.menu.SysMenuService;
import com.lesofn.archforge.user.api.service.SysRoleService;
import com.lesofn.archforge.user.api.service.SysUserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminLoginUserFactoryTest {

    @Mock
    private SysUserService userService;

    @Mock
    private SysMenuService menuService;

    @Mock
    private SysRoleService roleService;

    @Spy
    private ArchForgeProperties appForgeConfig = new ArchForgeProperties();

    @InjectMocks
    private AdminLoginUserFactory factory;

    @Test
    void loadUserNotFoundThrowsException() {
        when(userService.getUserByUserName("admin")).thenReturn(null);

        AdminUserException exception = assertThrows(AdminUserException.class, () -> factory.load("admin"));
        assertEquals(AdminUserErrorCode.USER_NON_EXIST.getCode(), exception.getErrorInfo().getCode());
    }

    @Test
    void loadDisabledUserThrowsException() {
        SysUser user = new SysUser();
        user.setUsername("admin");
        user.setStatus(UserStatusEnum.DISABLED.getValue());
        when(userService.getUserByUserName("admin")).thenReturn(user);

        AdminUserException exception = assertThrows(AdminUserException.class, () -> factory.load("admin"));
        assertEquals(AdminUserErrorCode.USER_IS_DISABLE.getCode(), exception.getErrorInfo().getCode());
    }

    @Test
    void loadNormalUserWithRole() {
        SysUser user = new SysUser();
        user.setUserId(1L);
        user.setUsername("admin");
        user.setPassword("encoded");
        user.setStatus(UserStatusEnum.NORMAL.getValue());
        user.setIsAdmin(false);
        user.setRoleId(2L);
        user.setDeptId(3L);

        SysRole role = new SysRole();
        role.setRoleId(2L);
        role.setRoleKey("manager");
        role.setDataScope((short) DataScopeEnum.ALL.getValue());
        role.setDeptIdSet("");

        SysMenu menu = new SysMenu();
        menu.setMenuId(10L);
        menu.setPermission("system:user:list");

        when(userService.getUserByUserName("admin")).thenReturn(user);
        when(roleService.getById(2L)).thenReturn(role);
        when(roleService.getMenuListByRoleId(2L)).thenReturn(List.of(menu));

        SystemLoginUser loginUser = factory.load("admin");
        assertEquals("admin", loginUser.getUsername());
        assertTrue(loginUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(loginUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("system:user:list")));
        assertEquals(2L, loginUser.getRoleId());
    }

    @Test
    void loadAdminUserHasAllPermissions() {
        SysUser user = new SysUser();
        user.setUserId(1L);
        user.setUsername("super");
        user.setPassword("encoded");
        user.setStatus(UserStatusEnum.NORMAL.getValue());
        user.setIsAdmin(true);
        user.setRoleId(2L);
        user.setDeptId(3L);

        SysMenu menu = new SysMenu();
        menu.setMenuId(10L);
        menu.setPermission("system:role:list");

        when(userService.getUserByUserName("super")).thenReturn(user);
        when(menuService.findAllActiveMenus()).thenReturn(List.of(menu));

        SystemLoginUser loginUser = factory.load("super");
        assertTrue(loginUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(loginUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(RoleInfo.ALL_PERMISSIONS)));
    }

    @Test
    void getRoleInfoReturnsEmptyWhenRoleIdNull() {
        assertSame(RoleInfo.EMPTY_ROLE, factory.getRoleInfo(null, false));
    }

    @Test
    void getRoleInfoReturnsEmptyWhenRoleMissing() {
        when(roleService.getById(99L)).thenReturn(null);
        assertSame(RoleInfo.EMPTY_ROLE, factory.getRoleInfo(99L, false));
    }
}
