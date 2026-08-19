package com.lesofn.archforge.server.admin.controller.permission;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import com.lesofn.archforge.server.admin.service.permission.PermissionMatrixService;
import com.lesofn.archforge.server.admin.service.permission.PermissionMenuNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "权限矩阵")
@SaCheckLogin(type = StpAdminUtil.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/permission-matrix")
public class PermissionMatrixController {

    private final PermissionMatrixService permissionMatrixService;

    @Operation(summary = "菜单权限树")
    @SaCheckPermission(value = "system:role:query", type = StpAdminUtil.TYPE)
    @GetMapping("/menus/tree")
    public List<PermissionMenuNode> menuTree() {
        return permissionMatrixService.menuTree();
    }

    @Operation(summary = "角色已授权菜单")
    @SaCheckPermission(value = "system:role:query", type = StpAdminUtil.TYPE)
    @GetMapping("/roles/{roleId}/permissions")
    public List<Long> rolePermissions(@PathVariable Long roleId) {
        return permissionMatrixService.rolePermissions(roleId);
    }

    @Operation(summary = "保存角色授权")
    @SaCheckPermission(value = "system:role:edit", type = StpAdminUtil.TYPE)
    @PutMapping("/roles/{roleId}/permissions")
    public void saveRolePermissions(@PathVariable Long roleId, @RequestBody @Valid PermissionUpdateRequest request) {
        permissionMatrixService.saveRolePermissions(roleId, request.getMenuIds());
    }

    @Data
    public static class PermissionUpdateRequest {
        @NotNull
        private List<Long> menuIds;
    }
}
