package com.lesofn.matrixboot.user.controller;

import com.lesofn.matrixboot.user.domain.SysRoleMenu;
import com.lesofn.matrixboot.user.service.SysRoleMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role-menus")
@RequiredArgsConstructor
public class SysRoleMenuController {

    private final SysRoleMenuService roleMenuService;

    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<SysRoleMenu>> getRoleMenusByRoleId(@PathVariable Long roleId) {
        List<SysRoleMenu> roleMenus = roleMenuService.findByRoleId(roleId);
        return ResponseEntity.ok(roleMenus);
    }

    @GetMapping("/menu/{menuId}")
    public ResponseEntity<List<SysRoleMenu>> getRoleMenusByMenuId(@PathVariable Long menuId) {
        List<SysRoleMenu> roleMenus = roleMenuService.findByMenuId(menuId);
        return ResponseEntity.ok(roleMenus);
    }

    @PostMapping
    public ResponseEntity<SysRoleMenu> createRoleMenu(@RequestBody SysRoleMenu roleMenu) {
        SysRoleMenu createdRoleMenu = roleMenuService.create(roleMenu);
        return ResponseEntity.ok(createdRoleMenu);
    }

    @PostMapping("/batch")
    public ResponseEntity<Void> createRoleMenusBatch(@RequestBody List<SysRoleMenu> roleMenus) {
        roleMenuService.createBatch(roleMenus);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/role/{roleId}/menus")
    public ResponseEntity<Void> updateRoleMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        roleMenuService.updateRoleMenus(roleId, menuIds);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/role/{roleId}")
    public ResponseEntity<Void> deleteRoleMenusByRoleId(@PathVariable Long roleId) {
        roleMenuService.deleteByRoleId(roleId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/menu/{menuId}")
    public ResponseEntity<Void> deleteRoleMenusByMenuId(@PathVariable Long menuId) {
        roleMenuService.deleteByMenuId(menuId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roleId}/{menuId}")
    public ResponseEntity<Void> deleteRoleMenu(@PathVariable Long roleId, @PathVariable Long menuId) {
        roleMenuService.deleteById(roleId, menuId);
        return ResponseEntity.ok().build();
    }
}