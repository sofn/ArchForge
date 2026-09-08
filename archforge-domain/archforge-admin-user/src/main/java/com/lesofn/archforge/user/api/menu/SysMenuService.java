package com.lesofn.archforge.user.api.menu;

import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.user.api.domain.SysMenu;
import com.lesofn.archforge.user.api.menu.dto.RouterDTO;
import java.util.List;
import java.util.Optional;

public interface SysMenuService {
    Optional<SysMenu> findById(Long id);

    List<SysMenu> findByParentId(Long parentId);

    List<SysMenu> findMenusByRoleId(Long roleId);

    List<SysMenu> findAllActiveMenus();

    List<SysMenu> findByPermission(String permission);

    SysMenu create(SysMenu menu);

    SysMenu update(SysMenu menu);

    void deleteById(Long id);

    void softDeleteById(Long id);

    List<SysMenu> buildMenuTree(List<SysMenu> menus);

    List<RouterDTO> getRouterTree(SystemLoginUser loginUser);
}
