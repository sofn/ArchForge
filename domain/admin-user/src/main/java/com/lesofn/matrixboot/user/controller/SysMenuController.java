package com.lesofn.matrixboot.user.controller;

import com.lesofn.matrixboot.user.domain.SysMenu;
import com.lesofn.matrixboot.user.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    @GetMapping("/{id}")
    public ResponseEntity<SysMenu> getMenuById(@PathVariable Long id) {
        return menuService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<SysMenu>> getMenusByParentId(@PathVariable Long parentId) {
        List<SysMenu> menus = menuService.findByParentId(parentId);
        return ResponseEntity.ok(menus);
    }

    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<SysMenu>> getMenusByRoleId(@PathVariable Long roleId) {
        List<SysMenu> menus = menuService.findMenusByRoleId(roleId);
        return ResponseEntity.ok(menus);
    }

    @GetMapping("/tree")
    public ResponseEntity<List<SysMenu>> getMenuTree() {
        List<SysMenu> menus = menuService.findAllActiveMenus();
        List<SysMenu> menuTree = menuService.buildMenuTree(menus);
        return ResponseEntity.ok(menuTree);
    }

    @GetMapping("/role/{roleId}/tree")
    public ResponseEntity<List<SysMenu>> getMenuTreeByRoleId(@PathVariable Long roleId) {
        List<SysMenu> menus = menuService.findMenusByRoleId(roleId);
        List<SysMenu> menuTree = menuService.buildMenuTree(menus);
        return ResponseEntity.ok(menuTree);
    }

    @GetMapping("/permission/{permission}")
    public ResponseEntity<List<SysMenu>> getMenusByPermission(@PathVariable String permission) {
        List<SysMenu> menus = menuService.findByPermission(permission);
        return ResponseEntity.ok(menus);
    }

    @GetMapping
    public ResponseEntity<List<SysMenu>> getAllActiveMenus() {
        List<SysMenu> menus = menuService.findAllActiveMenus();
        return ResponseEntity.ok(menus);
    }

    @PostMapping
    public ResponseEntity<SysMenu> createMenu(@RequestBody SysMenu menu) {
        SysMenu createdMenu = menuService.create(menu);
        return ResponseEntity.ok(createdMenu);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SysMenu> updateMenu(@PathVariable Long id, @RequestBody SysMenu menu) {
        menu.setMenuId(id);
        SysMenu updatedMenu = menuService.update(menu);
        return ResponseEntity.ok(updatedMenu);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateMenuStatus(@PathVariable Long id, @RequestParam Short status) {
        menuService.findById(id).ifPresent(menu -> {
            menu.setStatus(status);
            menuService.update(menu);
        });
        return ResponseEntity.ok().build();
    }
}