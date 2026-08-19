package com.lesofn.archforge.server.admin.controller.user;

import com.lesofn.archforge.infrastructure.annotation.Log;
import com.lesofn.archforge.server.admin.dto.AdminPageResponse;
import com.lesofn.archforge.server.admin.dto.AdminUserIdRequest;
import com.lesofn.archforge.server.admin.dto.AdminUserDTO;
import com.lesofn.archforge.server.admin.dto.AdminUserListRequest;
import com.lesofn.archforge.server.admin.dto.request.UserCreateRequest;
import com.lesofn.archforge.server.admin.dto.request.UserDeleteRequest;
import com.lesofn.archforge.server.admin.dto.request.UserPasswordRequest;
import com.lesofn.archforge.server.admin.dto.request.UserRoleRequest;
import com.lesofn.archforge.server.admin.dto.request.UserStatusRequest;
import com.lesofn.archforge.server.admin.dto.request.UserUpdateRequest;
import com.lesofn.archforge.server.admin.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端用户管理接口
 *
 * @author lesofn
 */
@Tag(name = "用户管理")
@SaCheckLogin(type = StpAdminUtil.TYPE)
@SaCheckRole(value = "ADMIN", type = StpAdminUtil.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/user")
public class UserController {

    private final AdminUserService userService;

    @Operation(summary = "获取用户列表")
    @SaCheckPermission(value = "system:user:list", type = StpAdminUtil.TYPE)
    @PostMapping
    public AdminPageResponse<AdminUserDTO> getUserList(@RequestBody AdminUserListRequest request) {
        return userService.getUserList(request);
    }

    @Operation(summary = "获取用户角色ID列表")
    @SaCheckPermission(value = "system:user:query", type = StpAdminUtil.TYPE)
    @PostMapping("/list-role-ids")
    public List<Long> listRoleIds(@RequestBody AdminUserIdRequest request) {
        if (request.getUserId() == null) {
            return Collections.emptyList();
        }
        return userService.getRoleIds(request.getUserId());
    }

    @Log
    @Operation(summary = "创建用户")
    @SaCheckPermission(value = "system:user:add", type = StpAdminUtil.TYPE)
    @PostMapping("/create")
    public Long createUser(@RequestBody @Valid UserCreateRequest request) {
        return userService.createUser(request);
    }

    @Log
    @Operation(summary = "更新用户")
    @SaCheckPermission(value = "system:user:edit", type = StpAdminUtil.TYPE)
    @PutMapping("/update")
    public Boolean updateUser(@RequestBody @Valid UserUpdateRequest request) {
        return userService.updateUser(request);
    }

    @Log
    @Operation(summary = "删除用户")
    @SaCheckPermission(value = "system:user:remove", type = StpAdminUtil.TYPE)
    @PostMapping("/delete")
    public Boolean deleteUser(@RequestBody @Valid UserDeleteRequest request) {
        return userService.deleteUser(request);
    }

    @Log
    @Operation(summary = "更新用户状态")
    @SaCheckPermission(value = "system:user:edit", type = StpAdminUtil.TYPE)
    @PostMapping("/status")
    public Boolean updateUserStatus(@RequestBody @Valid UserStatusRequest request) {
        return userService.updateStatus(request);
    }

    @Log
    @Operation(summary = "重置用户密码")
    @SaCheckPermission(value = "system:user:resetPwd", type = StpAdminUtil.TYPE)
    @PostMapping("/reset-password")
    public Boolean resetUserPassword(@RequestBody @Valid UserPasswordRequest request) {
        return userService.resetPassword(request);
    }

    @Log
    @Operation(summary = "分配用户角色")
    @SaCheckPermission(value = "system:user:edit", type = StpAdminUtil.TYPE)
    @PostMapping("/assign-role")
    public Boolean assignUserRole(@RequestBody @Valid UserRoleRequest request) {
        return userService.assignRole(request);
    }
}
