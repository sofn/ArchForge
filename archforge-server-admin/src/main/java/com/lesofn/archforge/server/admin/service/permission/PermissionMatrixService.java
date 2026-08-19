package com.lesofn.archforge.server.admin.service.permission;

import com.lesofn.archforge.user.api.domain.SysMenu;
import com.lesofn.archforge.user.api.domain.SysRoleMenu;
import com.lesofn.archforge.user.api.menu.SysMenuService;
import com.lesofn.archforge.user.api.service.SysRoleMenuService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionMatrixService {

    private final SysMenuService menuService;
    private final SysRoleMenuService roleMenuService;

    public List<PermissionMenuNode> menuTree() {
        List<SysMenu> menus = menuService.findAllActiveMenus();
        Map<Long, PermissionMenuNode> nodes = new LinkedHashMap<>();
        for (SysMenu menu : menus) {
            nodes.put(
                    menu.getMenuId(),
                    new PermissionMenuNode(menu.getMenuId(), menu.getParentId(), menu.getMenuName(), menu
                            .getPermission(), Boolean.TRUE.equals(menu.getIsButton()), new ArrayList<>()));
        }
        List<PermissionMenuNode> roots = new ArrayList<>();
        for (PermissionMenuNode node : nodes.values()) {
            PermissionMenuNode parent = node.parentId() == null ? null : nodes.get(node.parentId());
            if (parent == null || node.parentId() == 0L) {
                roots.add(node);
            } else {
                parent.children().add(node);
            }
        }
        return roots;
    }

    public List<Long> rolePermissions(Long roleId) {
        return roleMenuService.findByRoleId(roleId).stream().map(SysRoleMenu::getMenuId).toList();
    }

    public void saveRolePermissions(Long roleId, List<Long> menuIds) {
        roleMenuService.updateRoleMenus(roleId, menuIds != null ? menuIds : List.of());
    }
}
