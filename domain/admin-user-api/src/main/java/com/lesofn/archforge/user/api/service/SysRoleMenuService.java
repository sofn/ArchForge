package com.lesofn.archforge.user.api.service;

import com.lesofn.archforge.user.api.dao.SysRoleMenuRepository;
import com.lesofn.archforge.user.api.domain.SysRoleMenu;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public interface SysRoleMenuService {
    List<SysRoleMenu> findByRoleId(Long roleId);

    List<SysRoleMenu> findByMenuId(Long menuId);

    SysRoleMenu create(SysRoleMenu roleMenu);

    void createBatch(List<SysRoleMenu> roleMenus);

    void deleteByRoleId(Long roleId);

    void deleteByMenuId(Long menuId);

    void deleteById(Long roleId, Long menuId);

    void updateRoleMenus(Long roleId, List<Long> menuIds);
}
