package com.lesofn.archforge.server.admin.controller.system;

import com.lesofn.archforge.infrastructure.annotation.Log;
import com.lesofn.archforge.user.domain.SysUser;
import com.lesofn.archforge.user.domain.query.SysUserQuery;
import com.lesofn.archforge.user.service.SysUserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<SysUser> getUserById(@PathVariable Long id) {
        return userService
                .findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<SysUser> getUserByUsername(@PathVariable String username) {
        return userService
                .findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<SysUser>> getUsers(SysUserQuery query, Pageable pageable) {
        Page<SysUser> users = userService.searchUsers(query, pageable);
        return ResponseEntity.ok(users);
    }

    @Log(module = "用户管理", summary = "创建用户")
    @PostMapping
    public ResponseEntity<SysUser> createUser(@RequestBody SysUser user) {
        SysUser createdUser = userService.create(user);
        return ResponseEntity.ok(createdUser);
    }

    @Log(module = "用户管理", summary = "更新用户")
    @PutMapping("/{id}")
    public ResponseEntity<SysUser> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        user.setUserId(id);
        SysUser updatedUser = userService.update(user);
        return ResponseEntity.ok(updatedUser);
    }

    @Log(module = "用户管理", summary = "删除用户")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @Log(module = "用户管理", summary = "更新用户状态")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @Log(module = "用户管理", summary = "重置用户密码")
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long id, @RequestParam String newPassword) {
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
