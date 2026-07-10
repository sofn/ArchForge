package com.lesofn.archforge.server.admin.controller.admin;

import com.lesofn.archforge.server.admin.dto.AdminPageResult;
import com.lesofn.archforge.server.admin.dto.AdminRoleIdRequest;
import com.lesofn.archforge.server.admin.dto.AdminRoleItemDTO;
import com.lesofn.archforge.server.admin.dto.AdminRoleMenuItemDTO;
import com.lesofn.archforge.server.admin.dto.AdminRoleSimpleDTO;
import com.lesofn.archforge.server.admin.dto.AdminRoleListRequest;
import com.lesofn.archforge.server.admin.dto.request.RoleCreateRequest;
import com.lesofn.archforge.server.admin.dto.request.RoleDeleteRequest;
import com.lesofn.archforge.server.admin.dto.request.RoleMenuRequest;
import com.lesofn.archforge.server.admin.dto.request.RoleStatusRequest;
import com.lesofn.archforge.server.admin.dto.request.RoleUpdateRequest;
import com.lesofn.archforge.server.admin.mapper.AdminRoleMapper;
import com.lesofn.archforge.server.admin.mapper.AdminRoleMenuMapper;
import com.lesofn.archforge.user.dao.SysRoleMenuRepository;
import com.lesofn.archforge.user.domain.SysRole;
import com.lesofn.archforge.user.domain.SysRoleMenu;
import com.lesofn.archforge.user.menu.SysMenuService;
import com.lesofn.archforge.user.service.SysRoleMenuService;
import com.lesofn.archforge.user.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Tag(name = "角色管理")
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class AdminRoleController {

    private final SysRoleService roleService;
    private final SysRoleMenuService roleMenuService;
    private final SysRoleMenuRepository roleMenuRepository;
    private final SysMenuService menuService;
    private final AdminRoleMapper roleMapper;
    private final AdminRoleMenuMapper roleMenuMapper;

    @Operation(summary = "获取全量角色列表")
    @GetMapping("/list-all-role")
    public List<AdminRoleSimpleDTO> listAllRoles() {
        List<SysRole> roles = roleService.findAll();
        return roles.stream()
                .map(role -> AdminRoleSimpleDTO.of(role.getRoleId(), role.getRoleName()))
                .collect(Collectors.toList());
    }

    @Operation(summary = "获取角色列表")
    @PostMapping("/role")
    public AdminPageResult<AdminRoleItemDTO> getRoleList(@RequestBody AdminRoleListRequest request) {
        int currentPage = request.getCurrentPage() != null && request.getCurrentPage() > 0 ? request.getCurrentPage() : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0 ? request.getPageSize() : 10;

        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<SysRole> rolePage = roleService.findAll(pageable);

        List<AdminRoleItemDTO> roleItems = rolePage.getContent().stream()
                .filter(role -> !Boolean.TRUE.equals(role.getDeleted()))
                .map(roleMapper::toDto)
                .collect(Collectors.toList());

        return AdminPageResult.of(roleItems, rolePage.getTotalElements(), pageSize, currentPage);
    }

    @Operation(summary = "获取角色权限菜单树")
    @PostMapping("/role-menu")
    public List<AdminRoleMenuItemDTO> getRoleMenuTree() {
        List<com.lesofn.archforge.user.domain.SysMenu> allMenus = menuService.findAllActiveMenus();
        return allMenus.stream().map(roleMenuMapper::toDto).collect(Collectors.toList());
    }

    @Operation(summary = "获取角色菜单ID列表")
    @PostMapping("/role-menu-ids")
    public List<Long> getRoleMenuIds(@RequestBody AdminRoleIdRequest request) {
        if (request.getId() == null) {
            return Collections.emptyList();
        }
        List<SysRoleMenu> roleMenus = roleMenuRepository.findByRoleId(request.getId());
        return roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }

    @Operation(summary = "创建角色")
    @PostMapping("/role/create")
    public Long createRole(@RequestBody @Valid RoleCreateRequest request) {
        SysRole role = new SysRole();
        role.setRoleName(request.getName());
        role.setRoleKey(request.getCode());
        role.setRemark(request.getRemark() != null ? request.getRemark() : "");
        role.setStatus((short) 1);
        role.setRoleSort(0);
        SysRole saved = roleService.create(role);
        return saved.getRoleId();
    }

    @Operation(summary = "更新角色")
    @PutMapping("/role/update")
    public Boolean updateRole(@RequestBody @Valid RoleUpdateRequest request) {
        Optional<SysRole> opt = roleService.findById(request.getId());
        if (opt.isEmpty()) {
            return false;
        }
        SysRole role = opt.get();
        if (request.getName() != null)
            role.setRoleName(request.getName());
        if (request.getCode() != null)
            role.setRoleKey(request.getCode());
        if (request.getRemark() != null)
            role.setRemark(request.getRemark());
        roleService.update(role);
        return true;
    }

    @Operation(summary = "删除角色")
    @PostMapping("/role/delete")
    public Boolean deleteRole(@RequestBody @Valid RoleDeleteRequest request) {
        roleService.softDeleteById(request.getId());
        return true;
    }

    @Operation(summary = "更新角色状态")
    @PostMapping("/role/status")
    public Boolean updateRoleStatus(@RequestBody @Valid RoleStatusRequest request) {
        Optional<SysRole> opt = roleService.findById(request.getId());
        if (opt.isPresent()) {
            SysRole role = opt.get();
            role.setStatus(request.getStatus().shortValue());
            roleService.update(role);
        }
        return true;
    }

    @Operation(summary = "保存角色菜单权限")
    @PostMapping("/role/save-menu")
    public Boolean saveRoleMenu(@RequestBody @Valid RoleMenuRequest request) {
        List<Long> menuIdList = request.getMenuIds() != null ? request.getMenuIds() : Collections.emptyList();
        roleMenuService.updateRoleMenus(request.getId(), menuIdList);
        return true;
    }
}
