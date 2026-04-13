package com.lesofn.appforge.server.admin.controller;

import com.lesofn.appforge.server.admin.dto.*;
import com.lesofn.appforge.user.dao.SysRoleMenuRepository;
import com.lesofn.appforge.user.domain.SysDept;
import com.lesofn.appforge.user.domain.SysMenu;
import com.lesofn.appforge.user.domain.SysRole;
import com.lesofn.appforge.user.domain.SysRoleMenu;
import com.lesofn.appforge.user.domain.SysUser;
import com.lesofn.appforge.user.menu.SysMenuService;
import com.lesofn.appforge.user.menu.dto.ExtraIconDTO;
import com.lesofn.appforge.user.menu.dto.MetaDTO;
import com.lesofn.appforge.user.menu.dto.TransitionDTO;
import com.lesofn.appforge.user.service.SysDeptService;
import com.lesofn.appforge.user.service.SysRoleService;
import com.lesofn.appforge.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端API控制器，提供vue-pure-admin前端所需的系统管理接口
 *
 * @author lesofn
 */
@Slf4j
@Tag(name = "管理端API", description = "vue-pure-admin前端系统管理接口")
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class AdminApiController {

    private final SysUserService userService;
    private final SysRoleService roleService;
    private final SysMenuService menuService;
    private final SysRoleMenuRepository roleMenuRepository;

    @Autowired(required = false)
    private SysDeptService deptService;

    /**
     * 获取用户列表（分页）
     *
     * @param request 查询条件
     * @return 分页用户列表
     */
    @Operation(summary = "获取用户列表")
    @PostMapping("/user")
    public AdminPageResult<AdminUserItemDTO> getUserList(
            @RequestBody AdminUserListRequest request) {
        int currentPage =
                request.getCurrentPage() != null && request.getCurrentPage() > 0
                        ? request.getCurrentPage()
                        : 1;
        int pageSize =
                request.getPageSize() != null && request.getPageSize() > 0
                        ? request.getPageSize()
                        : 10;

        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<SysUser> userPage = userService.findAll(pageable);

        // 构建部门ID到名称的映射
        Map<Long, String> deptNameMap = buildDeptNameMap();

        List<AdminUserItemDTO> userItems =
                userPage.getContent().stream()
                        .map(user -> convertToUserItemDTO(user, deptNameMap))
                        .collect(Collectors.toList());

        return AdminPageResult.of(userItems, userPage.getTotalElements(), pageSize, currentPage);
    }

    /**
     * 获取全量角色列表
     *
     * @return 所有角色的简要信息
     */
    @Operation(summary = "获取全量角色列表")
    @GetMapping("/list-all-role")
    public List<AdminRoleSimpleDTO> listAllRoles() {
        List<SysRole> roles = roleService.findAll();
        return roles.stream()
                .map(role -> AdminRoleSimpleDTO.of(role.getRoleId(), role.getRoleName()))
                .collect(Collectors.toList());
    }

    /**
     * 获取用户的角色ID列表
     *
     * @param request 用户ID
     * @return 角色ID列表
     */
    @Operation(summary = "获取用户角色ID列表")
    @PostMapping("/list-role-ids")
    public List<Long> listRoleIds(@RequestBody AdminUserIdRequest request) {
        if (request.getUserId() == null) {
            return Collections.emptyList();
        }
        return userService
                .findById(request.getUserId())
                .map(
                        user -> {
                            List<Long> roleIds = new ArrayList<>();
                            if (user.getRoleId() != null) {
                                roleIds.add(user.getRoleId());
                            }
                            return roleIds;
                        })
                .orElse(Collections.emptyList());
    }

    /**
     * 获取角色列表（分页）
     *
     * @param request 查询条件
     * @return 分页角色列表
     */
    @Operation(summary = "获取角色列表")
    @PostMapping("/role")
    public AdminPageResult<AdminRoleItemDTO> getRoleList(
            @RequestBody AdminRoleListRequest request) {
        int currentPage =
                request.getCurrentPage() != null && request.getCurrentPage() > 0
                        ? request.getCurrentPage()
                        : 1;
        int pageSize =
                request.getPageSize() != null && request.getPageSize() > 0
                        ? request.getPageSize()
                        : 10;

        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<SysRole> rolePage = roleService.findAll(pageable);

        List<AdminRoleItemDTO> roleItems =
                rolePage.getContent().stream()
                        .map(this::convertToRoleItemDTO)
                        .collect(Collectors.toList());

        return AdminPageResult.of(roleItems, rolePage.getTotalElements(), pageSize, currentPage);
    }

    /**
     * 获取角色权限菜单树（全量菜单，供角色分配权限使用）
     *
     * @return 菜单列表（简化版）
     */
    @Operation(summary = "获取角色权限菜单树")
    @PostMapping("/role-menu")
    public List<AdminRoleMenuItemDTO> getRoleMenuTree() {
        List<SysMenu> allMenus = menuService.findAllActiveMenus();
        return allMenus.stream().map(this::convertToRoleMenuItemDTO).collect(Collectors.toList());
    }

    /**
     * 获取角色已分配的菜单ID列表
     *
     * @param request 角色ID
     * @return 菜单ID列表
     */
    @Operation(summary = "获取角色菜单ID列表")
    @PostMapping("/role-menu-ids")
    public List<Long> getRoleMenuIds(@RequestBody AdminRoleIdRequest request) {
        if (request.getId() == null) {
            return Collections.emptyList();
        }
        List<SysRoleMenu> roleMenus = roleMenuRepository.findByRoleId(request.getId());
        return roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }

    /**
     * 获取全量菜单列表（扁平结构，前端构建树）
     *
     * @return 菜单列表
     */
    @Operation(summary = "获取全量菜单列表")
    @PostMapping("/menu")
    public List<AdminMenuItemDTO> getMenuList() {
        List<SysMenu> allMenus = menuService.findAllActiveMenus();
        return allMenus.stream().map(this::convertToMenuItemDTO).collect(Collectors.toList());
    }

    /**
     * 获取全量部门列表（扁平结构，前端构建树）
     *
     * @return 部门列表
     */
    @Operation(summary = "获取全量部门列表")
    @PostMapping("/dept")
    public List<AdminDeptItemDTO> getDeptList() {
        if (deptService == null) {
            return Collections.emptyList();
        }
        List<SysDept> allDepts = deptService.findAll();
        return allDepts.stream().map(this::convertToDeptItemDTO).collect(Collectors.toList());
    }

    // ==================== 私有转换方法 ====================

    private AdminUserItemDTO convertToUserItemDTO(SysUser user, Map<Long, String> deptNameMap) {
        AdminUserItemDTO dto = new AdminUserItemDTO();
        dto.setId(user.getUserId());
        dto.setAvatar(user.getAvatar());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setPhone(user.getPhoneNumber());
        dto.setEmail(user.getEmail());
        dto.setSex(user.getSex() != null ? user.getSex().getValue() : null);
        dto.setStatus(user.getStatus());
        dto.setRemark(user.getRemark());
        dto.setCreateTime(toEpochMilli(user.getCreateTime()));

        // 设置部门信息
        if (user.getDeptId() != null) {
            String deptName = deptNameMap.getOrDefault(user.getDeptId(), "");
            dto.setDept(AdminUserItemDTO.DeptInfo.of(user.getDeptId(), deptName));
        }

        return dto;
    }

    private AdminRoleItemDTO convertToRoleItemDTO(SysRole role) {
        AdminRoleItemDTO dto = new AdminRoleItemDTO();
        dto.setId(role.getRoleId());
        dto.setName(role.getRoleName());
        dto.setCode(role.getRoleKey());
        dto.setStatus(role.getStatus() != null ? role.getStatus().intValue() : null);
        dto.setRemark(role.getRemark());
        dto.setCreateTime(toEpochMilli(role.getCreateTime()));
        dto.setUpdateTime(toEpochMilli(role.getUpdateTime()));
        return dto;
    }

    private AdminRoleMenuItemDTO convertToRoleMenuItemDTO(SysMenu menu) {
        AdminRoleMenuItemDTO dto = new AdminRoleMenuItemDTO();
        dto.setParentId(menu.getParentId());
        dto.setId(menu.getMenuId());
        dto.setMenuType(menu.getMenuType());

        MetaDTO meta = menu.getMetaInfo();
        if (meta != null) {
            dto.setTitle(meta.getTitle());
        }

        return dto;
    }

    private AdminMenuItemDTO convertToMenuItemDTO(SysMenu menu) {
        AdminMenuItemDTO dto = new AdminMenuItemDTO();
        dto.setParentId(menu.getParentId());
        dto.setId(menu.getMenuId());
        dto.setMenuType(menu.getMenuType());
        dto.setName(menu.getRouterName());
        dto.setPath(menu.getPath());
        dto.setComponent("");
        dto.setRedirect("");
        dto.setActivePath("");
        dto.setAuths(menu.getPermission() != null ? menu.getPermission() : "");
        dto.setFixedTag(false);

        MetaDTO meta = menu.getMetaInfo();
        if (meta != null) {
            dto.setTitle(meta.getTitle());
            dto.setRank(meta.getRank());
            dto.setIcon(meta.getIcon() != null ? meta.getIcon() : "");
            dto.setFrameSrc(meta.getFrameSrc() != null ? meta.getFrameSrc() : "");
            dto.setFrameLoading(meta.getFrameLoading() != null ? meta.getFrameLoading() : true);
            dto.setKeepAlive(meta.getKeepAlive() != null ? meta.getKeepAlive() : false);
            dto.setHiddenTag(meta.getHiddenTag() != null ? meta.getHiddenTag() : false);
            dto.setShowLink(meta.getShowLink() != null ? meta.getShowLink() : true);
            dto.setShowParent(meta.getShowParent() != null ? meta.getShowParent() : false);

            ExtraIconDTO extraIcon = meta.getExtraIcon();
            dto.setExtraIcon(extraIcon != null ? extraIcon.getName() : "");

            TransitionDTO transition = meta.getTransition();
            if (transition != null) {
                dto.setEnterTransition(
                        transition.getEnterTransition() != null
                                ? transition.getEnterTransition()
                                : "");
                dto.setLeaveTransition(
                        transition.getLeaveTransition() != null
                                ? transition.getLeaveTransition()
                                : "");
            } else {
                dto.setEnterTransition("");
                dto.setLeaveTransition("");
            }
        } else {
            dto.setTitle("");
            dto.setRank(0);
            dto.setIcon("");
            dto.setExtraIcon("");
            dto.setEnterTransition("");
            dto.setLeaveTransition("");
            dto.setFrameSrc("");
            dto.setFrameLoading(true);
            dto.setKeepAlive(false);
            dto.setHiddenTag(false);
            dto.setShowLink(true);
            dto.setShowParent(false);
        }

        return dto;
    }

    private AdminDeptItemDTO convertToDeptItemDTO(SysDept dept) {
        AdminDeptItemDTO dto = new AdminDeptItemDTO();
        dto.setId(dept.getDeptId());
        dto.setParentId(dept.getParentId());
        dto.setName(dept.getName());
        dto.setPrincipal(dept.getPrincipal());
        dto.setPhone(dept.getPhone());
        dto.setEmail(dept.getEmail());
        dto.setSort(dept.getSort());
        dto.setStatus(dept.getStatus());
        dto.setType(dept.getType());
        dto.setRemark(dept.getRemark());
        dto.setCreateTime(toEpochMilli(dept.getCreateTime()));
        return dto;
    }

    private Map<Long, String> buildDeptNameMap() {
        if (deptService == null) {
            return Collections.emptyMap();
        }
        List<SysDept> allDepts = deptService.findAll();
        return allDepts.stream()
                .collect(Collectors.toMap(SysDept::getDeptId, SysDept::getName, (a, b) -> a));
    }

    private Long toEpochMilli(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
