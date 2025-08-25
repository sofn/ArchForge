package com.lesofn.matrixboot.user.rest;

import com.lesofn.matrixboot.user.domain.SysUser;
import com.lesofn.matrixboot.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 用户REST控制器
 *
 * @author lesofn
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 根据用户名查询用户
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<SysUser> getUserByUsername(@PathVariable String username) {
        Optional<SysUser> user = userService.findByUsername(username);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 查询所有用户
     */
    @GetMapping
    public ResponseEntity<List<SysUser>> getAllUsers() {
        List<SysUser> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 创建用户
     */
    @PostMapping
    public ResponseEntity<SysUser> createUser(@RequestBody SysUser user) {
        SysUser savedUser = userService.saveUser(user);
        return ResponseEntity.ok(savedUser);
    }
}