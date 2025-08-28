package com.lesofn.matrixboot.deploy.controller.system;

import com.lesofn.matrixboot.user.domain.SysUser;
import com.lesofn.matrixboot.user.service.SysUserService;
import com.lesofn.matrixboot.user.domain.query.SysUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<SysUser> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<SysUser> getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<SysUser>> getUsers(SysUserQuery query, Pageable pageable) {
        Page<SysUser> users = userService.searchUsers(query, pageable);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<SysUser> createUser(@RequestBody SysUser user) {
        SysUser createdUser = userService.create(user);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SysUser> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        user.setUserId(id);
        SysUser updatedUser = userService.update(user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/active")
    public ResponseEntity<List<SysUser>> getActiveUsers() {
        List<SysUser> users = userService.findActiveUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/dept/{deptId}")
    public ResponseEntity<List<SysUser>> getUsersByDeptId(@PathVariable Long deptId) {
        List<SysUser> users = userService.findByDeptId(deptId);
        return ResponseEntity.ok(users);
    }
}