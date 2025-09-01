package com.lesofn.appboot.user.service;

import com.lesofn.appboot.user.dao.SysRoleMenuRepository;
import com.lesofn.appboot.user.domain.SysRoleMenu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleMenuService {

    private final SysRoleMenuRepository roleMenuRepository;

    public List<SysRoleMenu> findByRoleId(Long roleId) {
        return roleMenuRepository.findByRoleId(roleId);
    }

    public List<SysRoleMenu> findByMenuId(Long menuId) {
        return roleMenuRepository.findByMenuId(menuId);
    }

    @Transactional
    public SysRoleMenu create(SysRoleMenu roleMenu) {
        return roleMenuRepository.save(roleMenu);
    }

    @Transactional
    public void createBatch(List<SysRoleMenu> roleMenus) {
        roleMenuRepository.saveAll(roleMenus);
    }

    @Transactional
    public void deleteByRoleId(Long roleId) {
        roleMenuRepository.deleteByRoleId(roleId);
    }

    @Transactional
    public void deleteByMenuId(Long menuId) {
        roleMenuRepository.deleteByMenuId(menuId);
    }

    @Transactional
    public void deleteById(Long roleId, Long menuId) {
        SysRoleMenu.SysRoleMenuId id = new SysRoleMenu.SysRoleMenuId();
        id.setRoleId(roleId);
        id.setMenuId(menuId);
        roleMenuRepository.deleteById(id);
    }

    @Transactional
    public void updateRoleMenus(Long roleId, List<Long> menuIds) {
        roleMenuRepository.deleteByRoleId(roleId);
        
        List<SysRoleMenu> roleMenus = menuIds.stream()
                .map(menuId -> {
                    SysRoleMenu roleMenu = new SysRoleMenu();
                    roleMenu.setRoleId(roleId);
                    roleMenu.setMenuId(menuId);
                    return roleMenu;
                })
                .toList();
        
        roleMenuRepository.saveAll(roleMenus);
    }
}