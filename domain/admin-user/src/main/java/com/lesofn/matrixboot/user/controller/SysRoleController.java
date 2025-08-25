package com.lesofn.matrixboot.user.controller;

import com.lesofn.matrixboot.user.domain.SysRole;
import com.lesofn.matrixboot.user.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @GetMapping("/{id}")
    public ResponseEntity<SysRole> getRoleById(@PathVariable Long id) {
        return roleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/key/{roleKey}")
    public ResponseEntity<SysRole> getRoleByKey(@PathVariable String roleKey) {
        return roleService.findByRoleKey(roleKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{roleName}")
    public ResponseEntity<SysRole> getRoleByName(@PathVariable String roleName) {
        return roleService.findByRoleName(roleName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<SysRole>> getAllRoles(Pageable pageable) {
        Page<SysRole> roles = roleService.findAll(pageable);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SysRole>> getAllRolesList() {
        List<SysRole> roles = roleService.findAll();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/active")
    public ResponseEntity<List<SysRole>> getActiveRoles() {
        List<SysRole> roles = roleService.findAllActiveRoles();
        return ResponseEntity.ok(roles);
    }

    @PostMapping
    public ResponseEntity<SysRole> createRole(@RequestBody SysRole role) {
        SysRole createdRole = roleService.create(role);
        return ResponseEntity.ok(createdRole);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SysRole> updateRole(@PathVariable Long id, @RequestBody SysRole role) {
        role.setRoleId(id);
        SysRole updatedRole = roleService.update(role);
        return ResponseEntity.ok(updatedRole);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/soft-delete")
    public ResponseEntity<Void> softDeleteRole(@PathVariable Long id) {
        roleService.softDeleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/exists/key")
    public ResponseEntity<Boolean> checkRoleKeyExists(@RequestParam String roleKey) {
        boolean exists = roleService.existsByRoleKey(roleKey);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists/name")
    public ResponseEntity<Boolean> checkRoleNameExists(@RequestParam String roleName) {
        boolean exists = roleService.existsByRoleName(roleName);
        return ResponseEntity.ok(exists);
    }
}