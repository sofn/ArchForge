package com.lesofn.matrixboot.user.service;

import com.lesofn.matrixboot.user.dao.SysMenuRepository;
import com.lesofn.matrixboot.user.dao.SysRoleMenuRepository;
import com.lesofn.matrixboot.user.domain.SysMenu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuRepository menuRepository;
    private final SysRoleMenuRepository roleMenuRepository;

    public Optional<SysMenu> findById(Long id) {
        return menuRepository.findById(id);
    }

    public List<SysMenu> findByParentId(Long parentId) {
        return menuRepository.findByParentId(parentId);
    }

    public List<SysMenu> findMenusByRoleId(Long roleId) {
        return menuRepository.findMenusByRoleId(roleId);
    }

    public List<SysMenu> findAllActiveMenus() {
        return menuRepository.findAllActiveMenus();
    }

    public List<SysMenu> findByPermission(String permission) {
        return menuRepository.findByPermission(permission);
    }

    @Transactional
    public SysMenu create(SysMenu menu) {
        menu.setCreateTime(LocalDateTime.now());
        menu.setDeleted(false);
        return menuRepository.save(menu);
    }

    @Transactional
    public SysMenu update(SysMenu menu) {
        menu.setUpdateTime(LocalDateTime.now());
        return menuRepository.save(menu);
    }

    @Transactional
    public void deleteById(Long id) {
        menuRepository.deleteById(id);
        roleMenuRepository.deleteByMenuId(id);
    }

    @Transactional
    public void softDeleteById(Long id) {
        menuRepository.findById(id).ifPresent(menu -> {
            menu.setDeleted(true);
            menu.setUpdateTime(LocalDateTime.now());
            menuRepository.save(menu);
        });
    }

    public List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        return buildMenuTree(menus, 0L);
    }

    private List<SysMenu> buildMenuTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> menu.getParentId().equals(parentId))
                .peek(menu -> menu.setChildren(buildMenuTree(menus, menu.getMenuId())))
                .toList();
    }

    public List<SysMenu> getMenuListByRoleId(Long roleId) {
        return null;
    }
}