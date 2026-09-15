package com.lesofn.archforge.user.internal.service;

import com.lesofn.archforge.user.api.service.SysRoleMenuService;
import com.lesofn.archforge.user.api.dao.SysRoleMenuRepository;
import com.lesofn.archforge.user.api.domain.SysRoleMenu;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysRoleMenuServiceImpl implements SysRoleMenuService {

    private final SysRoleMenuRepository roleMenuRepository;

    @Override
    public List<SysRoleMenu> findByRoleId(Long roleId) {
        return roleMenuRepository.findByRoleId(roleId);
    }

    @Override
    public List<SysRoleMenu> findByMenuId(Long menuId) {
        return roleMenuRepository.findByMenuId(menuId);
    }

    @Override
    @Transactional
    public SysRoleMenu create(SysRoleMenu roleMenu) {
        return roleMenuRepository.save(roleMenu);
    }

    @Override
    @Transactional
    public void createBatch(List<SysRoleMenu> roleMenus) {
        roleMenuRepository.saveAll(roleMenus);
    }

    @Override
    @Transactional
    public void deleteByRoleId(Long roleId) {
        roleMenuRepository.deleteByRoleId(roleId);
    }

    @Override
    @Transactional
    public void deleteByMenuId(Long menuId) {
        roleMenuRepository.deleteByMenuId(menuId);
    }

    @Override
    @Transactional
    public void deleteById(Long roleId, Long menuId) {
        SysRoleMenu.SysRoleMenuId id = new SysRoleMenu.SysRoleMenuId();
        id.setRoleId(roleId);
        id.setMenuId(menuId);
        roleMenuRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateRoleMenus(Long roleId, List<Long> menuIds) {
        roleMenuRepository.deleteByRoleId(roleId);

        List<SysRoleMenu> roleMenus = menuIds.stream()
                .map(
                        menuId -> {
                            SysRoleMenu roleMenu = new SysRoleMenu();
                            roleMenu.setRoleId(roleId);
                            roleMenu.setMenuId(menuId);
                            return roleMenu;
                        })
                .toList();

        roleMenuRepository.saveAll(roleMenus);
    }
}
