package com.lesofn.archforge.server.admin.service.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.user.api.domain.SysMenu;
import com.lesofn.archforge.user.api.domain.SysRoleMenu;
import com.lesofn.archforge.user.api.menu.SysMenuService;
import com.lesofn.archforge.user.api.service.SysRoleMenuService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PermissionMatrixServiceTest {

    @Mock
    private SysMenuService menuService;

    @Mock
    private SysRoleMenuService roleMenuService;

    @InjectMocks
    private PermissionMatrixService permissionMatrixService;

    @Test
    void rolePermissionsReturnAssignedMenuIds() {
        SysRoleMenu assigned = new SysRoleMenu();
        assigned.setRoleId(9L);
        assigned.setMenuId(21L);
        when(roleMenuService.findByRoleId(9L)).thenReturn(List.of(assigned));

        assertEquals(List.of(21L), permissionMatrixService.rolePermissions(9L));
    }

    @Test
    void saveRolePermissionsDelegatesToRoleMenuService() {
        permissionMatrixService.saveRolePermissions(3L, List.of(1L, 2L));
        verify(roleMenuService).updateRoleMenus(3L, List.of(1L, 2L));
    }

    @Test
    void menuTreeUsesActiveMenus() {
        SysMenu menu = new SysMenu();
        menu.setMenuId(1L);
        menu.setMenuName("Users");
        when(menuService.findAllActiveMenus()).thenReturn(List.of(menu));

        assertEquals(1, permissionMatrixService.menuTree().size());
        assertEquals("Users", permissionMatrixService.menuTree().get(0).name());
    }
}
