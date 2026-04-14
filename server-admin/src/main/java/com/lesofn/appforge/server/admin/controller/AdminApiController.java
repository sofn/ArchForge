package com.lesofn.appforge.server.admin.controller;

import com.lesofn.appforge.common.enums.common.GenderEnum;
import com.lesofn.appforge.server.admin.dto.*;
import com.lesofn.appforge.server.admin.service.monitor.ServerMonitorService;
import com.lesofn.appforge.user.dao.SysRoleMenuRepository;
import com.lesofn.appforge.user.domain.SysConfig;
import com.lesofn.appforge.user.domain.SysDept;
import com.lesofn.appforge.user.domain.SysLoginLog;
import com.lesofn.appforge.user.domain.SysMenu;
import com.lesofn.appforge.user.domain.SysNotice;
import com.lesofn.appforge.user.domain.SysOperLog;
import com.lesofn.appforge.user.domain.SysRole;
import com.lesofn.appforge.user.domain.SysRoleMenu;
import com.lesofn.appforge.user.domain.SysUser;
import com.lesofn.appforge.user.menu.SysMenuService;
import com.lesofn.appforge.user.menu.dto.ExtraIconDTO;
import com.lesofn.appforge.user.menu.dto.MetaDTO;
import com.lesofn.appforge.user.menu.dto.TransitionDTO;
import com.lesofn.appforge.user.service.SysConfigService;
import com.lesofn.appforge.user.service.SysDeptService;
import com.lesofn.appforge.user.service.SysLoginLogService;
import com.lesofn.appforge.user.service.SysNoticeService;
import com.lesofn.appforge.user.service.SysOperLogService;
import com.lesofn.appforge.user.service.SysRoleMenuService;
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
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final SysRoleMenuService roleMenuService;
    private final SysConfigService configService;
    private final SysNoticeService noticeService;
    private final SysOperLogService operLogService;
    private final SysLoginLogService loginLogService;
    private final PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private SysDeptService deptService;

    @Autowired(required = false)
    private ServerMonitorService serverMonitorService;

    // ==================== 列表查询 ====================

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
        Map<Long, String> deptNameMap = buildDeptNameMap();

        // Apply filters: deptId, username, phone, status
        List<AdminUserItemDTO> userItems =
                userPage.getContent().stream()
                        .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
                        .filter(
                                user -> {
                                    Long deptId = request.getDeptIdAsLong();
                                    if (deptId != null) {
                                        return deptId.equals(user.getDeptId());
                                    }
                                    return true;
                                })
                        .filter(
                                user -> {
                                    String q = request.getUsername();
                                    if (q != null && !q.isEmpty()) {
                                        return user.getUsername() != null
                                                && user.getUsername().contains(q);
                                    }
                                    return true;
                                })
                        .filter(
                                user -> {
                                    String q = request.getPhone();
                                    if (q != null && !q.isEmpty()) {
                                        return user.getPhoneNumber() != null
                                                && user.getPhoneNumber().contains(q);
                                    }
                                    return true;
                                })
                        .filter(
                                user -> {
                                    Integer q = request.getStatusAsInt();
                                    if (q != null) {
                                        return q.equals(user.getStatus());
                                    }
                                    return true;
                                })
                        .map(user -> convertToUserItemDTO(user, deptNameMap))
                        .collect(Collectors.toList());

        return AdminPageResult.of(userItems, (long) userItems.size(), pageSize, currentPage);
    }

    @Operation(summary = "获取全量角色列表")
    @GetMapping("/list-all-role")
    public List<AdminRoleSimpleDTO> listAllRoles() {
        List<SysRole> roles = roleService.findAll();
        return roles.stream()
                .map(role -> AdminRoleSimpleDTO.of(role.getRoleId(), role.getRoleName()))
                .collect(Collectors.toList());
    }

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
                        .filter(role -> !Boolean.TRUE.equals(role.getDeleted()))
                        .map(this::convertToRoleItemDTO)
                        .collect(Collectors.toList());

        return AdminPageResult.of(roleItems, rolePage.getTotalElements(), pageSize, currentPage);
    }

    @Operation(summary = "获取角色权限菜单树")
    @PostMapping("/role-menu")
    public List<AdminRoleMenuItemDTO> getRoleMenuTree() {
        List<SysMenu> allMenus = menuService.findAllActiveMenus();
        return allMenus.stream().map(this::convertToRoleMenuItemDTO).collect(Collectors.toList());
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

    @Operation(summary = "获取全量菜单列表")
    @PostMapping("/menu")
    public List<AdminMenuItemDTO> getMenuList() {
        List<SysMenu> allMenus = menuService.findAllActiveMenus();
        return allMenus.stream().map(this::convertToMenuItemDTO).collect(Collectors.toList());
    }

    @Operation(summary = "获取全量部门列表")
    @PostMapping("/dept")
    public List<AdminDeptItemDTO> getDeptList() {
        if (deptService == null) {
            return Collections.emptyList();
        }
        List<SysDept> allDepts = deptService.findAll();
        return allDepts.stream().map(this::convertToDeptItemDTO).collect(Collectors.toList());
    }

    // ==================== User CRUD ====================

    @Operation(summary = "创建用户")
    @PostMapping("/user/create")
    public Long createUser(@RequestBody Map<String, Object> data) {
        SysUser user = new SysUser();
        user.setUsername((String) data.get("username"));
        user.setNickname((String) data.get("nickname"));
        user.setPhoneNumber(String.valueOf(data.getOrDefault("phone", "")));
        user.setEmail((String) data.getOrDefault("email", ""));
        if (data.get("sex") != null) {
            user.setSex(GenderEnum.fromValue(((Number) data.get("sex")).intValue()));
        }
        user.setStatus((Integer) data.getOrDefault("status", 1));
        user.setRemark((String) data.getOrDefault("remark", ""));
        if (data.get("parentId") != null) {
            user.setDeptId(((Number) data.get("parentId")).longValue());
        }
        String rawPassword = (String) data.getOrDefault("password", "admin123");
        user.setPassword(passwordEncoder.encode(rawPassword));
        SysUser saved = userService.create(user);
        return saved.getUserId();
    }

    @Operation(summary = "更新用户")
    @PutMapping("/user/update")
    public Boolean updateUser(@RequestBody Map<String, Object> data) {
        Long id = ((Number) data.get("id")).longValue();
        Optional<SysUser> opt = userService.findById(id);
        if (opt.isEmpty()) return false;
        SysUser user = opt.get();
        if (data.containsKey("username")) user.setUsername((String) data.get("username"));
        if (data.containsKey("nickname")) user.setNickname((String) data.get("nickname"));
        if (data.containsKey("phone")) user.setPhoneNumber(String.valueOf(data.get("phone")));
        if (data.containsKey("email")) user.setEmail((String) data.get("email"));
        if (data.get("sex") != null)
            user.setSex(GenderEnum.fromValue(((Number) data.get("sex")).intValue()));
        if (data.containsKey("status")) user.setStatus((Integer) data.get("status"));
        if (data.containsKey("remark")) user.setRemark((String) data.get("remark"));
        if (data.get("parentId") != null)
            user.setDeptId(((Number) data.get("parentId")).longValue());
        userService.update(user);
        return true;
    }

    @Operation(summary = "删除用户")
    @PostMapping("/user/delete")
    public Boolean deleteUser(@RequestBody Map<String, Object> data) {
        Object idObj = data.get("id");
        if (idObj instanceof Number) {
            userService.softDeleteById(((Number) idObj).longValue());
        }
        return true;
    }

    @Operation(summary = "更新用户状态")
    @PostMapping("/user/status")
    public Boolean updateUserStatus(@RequestBody Map<String, Object> data) {
        Long id = ((Number) data.get("id")).longValue();
        Integer status = (Integer) data.get("status");
        userService.updateStatus(id, status);
        return true;
    }

    @Operation(summary = "重置用户密码")
    @PostMapping("/user/reset-password")
    public Boolean resetUserPassword(@RequestBody Map<String, Object> data) {
        Long id = ((Number) data.get("id")).longValue();
        String newPwd = (String) data.getOrDefault("newPwd", "admin123");
        userService.resetPassword(id, passwordEncoder.encode(newPwd));
        return true;
    }

    @SuppressWarnings("unchecked")
    @Operation(summary = "分配用户角色")
    @PostMapping("/user/assign-role")
    public Boolean assignUserRole(@RequestBody Map<String, Object> data) {
        Long userId = ((Number) data.get("id")).longValue();
        List<Number> ids = (List<Number>) data.get("ids");
        if (ids != null && !ids.isEmpty()) {
            Long roleId = ids.get(0).longValue();
            Optional<SysUser> opt = userService.findById(userId);
            if (opt.isPresent()) {
                SysUser user = opt.get();
                user.setRoleId(roleId);
                userService.update(user);
            }
        }
        return true;
    }

    // ==================== Role CRUD ====================

    @Operation(summary = "创建角色")
    @PostMapping("/role/create")
    public Long createRole(@RequestBody Map<String, Object> data) {
        SysRole role = new SysRole();
        role.setRoleName((String) data.get("name"));
        role.setRoleKey((String) data.get("code"));
        role.setRemark((String) data.getOrDefault("remark", ""));
        role.setStatus((short) 1);
        role.setRoleSort(0);
        SysRole saved = roleService.create(role);
        return saved.getRoleId();
    }

    @Operation(summary = "更新角色")
    @PutMapping("/role/update")
    public Boolean updateRole(@RequestBody Map<String, Object> data) {
        Long id = ((Number) data.get("id")).longValue();
        Optional<SysRole> opt = roleService.findById(id);
        if (opt.isEmpty()) return false;
        SysRole role = opt.get();
        if (data.containsKey("name")) role.setRoleName((String) data.get("name"));
        if (data.containsKey("code")) role.setRoleKey((String) data.get("code"));
        if (data.containsKey("remark")) role.setRemark((String) data.get("remark"));
        roleService.update(role);
        return true;
    }

    @Operation(summary = "删除角色")
    @PostMapping("/role/delete")
    public Boolean deleteRole(@RequestBody Map<String, Object> data) {
        Long id = ((Number) data.get("id")).longValue();
        roleService.softDeleteById(id);
        return true;
    }

    @Operation(summary = "更新角色状态")
    @PostMapping("/role/status")
    public Boolean updateRoleStatus(@RequestBody Map<String, Object> data) {
        Long id = ((Number) data.get("id")).longValue();
        Integer status = (Integer) data.get("status");
        Optional<SysRole> opt = roleService.findById(id);
        if (opt.isPresent()) {
            SysRole role = opt.get();
            role.setStatus(status.shortValue());
            roleService.update(role);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Operation(summary = "保存角色菜单权限")
    @PostMapping("/role/save-menu")
    public Boolean saveRoleMenu(@RequestBody Map<String, Object> data) {
        Long roleId = ((Number) data.get("id")).longValue();
        List<Number> menuIds = (List<Number>) data.get("menuIds");
        List<Long> menuIdList =
                menuIds != null
                        ? menuIds.stream().map(Number::longValue).collect(Collectors.toList())
                        : Collections.emptyList();
        roleMenuService.updateRoleMenus(roleId, menuIdList);
        return true;
    }

    // ==================== Menu CRUD ====================

    @Operation(summary = "创建菜单")
    @PostMapping("/menu/create")
    public Long createMenu(@RequestBody Map<String, Object> data) {
        SysMenu menu = buildMenuFromData(data);
        SysMenu saved = menuService.create(menu);
        return saved.getMenuId();
    }

    @Operation(summary = "更新菜单")
    @PutMapping("/menu/update")
    public Boolean updateMenu(@RequestBody Map<String, Object> data) {
        Long id = ((Number) data.get("id")).longValue();
        Optional<SysMenu> opt = menuService.findById(id);
        if (opt.isEmpty()) return false;
        SysMenu menu = opt.get();
        updateMenuFromData(menu, data);
        menuService.update(menu);
        return true;
    }

    @Operation(summary = "删除菜单")
    @PostMapping("/menu/delete")
    public Boolean deleteMenu(@RequestBody Map<String, Object> data) {
        Long id = ((Number) data.get("id")).longValue();
        menuService.softDeleteById(id);
        return true;
    }

    // ==================== Dept CRUD ====================

    @Operation(summary = "创建部门")
    @PostMapping("/dept/create")
    public Long createDept(@RequestBody Map<String, Object> data) {
        SysDept dept = new SysDept();
        dept.setParentId(
                data.get("parentId") != null ? ((Number) data.get("parentId")).longValue() : 0L);
        dept.setName((String) data.get("name"));
        dept.setPrincipal((String) data.getOrDefault("principal", ""));
        dept.setPhone(String.valueOf(data.getOrDefault("phone", "")));
        dept.setEmail((String) data.getOrDefault("email", ""));
        dept.setSort(data.get("sort") != null ? ((Number) data.get("sort")).intValue() : 0);
        dept.setStatus((Integer) data.getOrDefault("status", 1));
        dept.setRemark((String) data.getOrDefault("remark", ""));
        SysDept saved = deptService.create(dept);
        return saved.getDeptId();
    }

    @Operation(summary = "更新部门")
    @PutMapping("/dept/update")
    public Boolean updateDept(@RequestBody Map<String, Object> data) {
        Long id = ((Number) data.get("id")).longValue();
        Optional<SysDept> opt = deptService.findById(id);
        if (opt.isEmpty()) return false;
        SysDept dept = opt.get();
        if (data.containsKey("name")) dept.setName((String) data.get("name"));
        if (data.containsKey("principal")) dept.setPrincipal((String) data.get("principal"));
        if (data.containsKey("phone")) dept.setPhone(String.valueOf(data.get("phone")));
        if (data.containsKey("email")) dept.setEmail((String) data.get("email"));
        if (data.get("sort") != null) dept.setSort(((Number) data.get("sort")).intValue());
        if (data.containsKey("status")) dept.setStatus((Integer) data.get("status"));
        if (data.containsKey("remark")) dept.setRemark((String) data.get("remark"));
        if (data.get("parentId") != null)
            dept.setParentId(((Number) data.get("parentId")).longValue());
        deptService.update(dept);
        return true;
    }

    @Operation(summary = "删除部门")
    @PostMapping("/dept/delete")
    public Boolean deleteDept(@RequestBody Map<String, Object> data) {
        Long id = ((Number) data.get("id")).longValue();
        deptService.deleteById(id);
        return true;
    }

    // ==================== Config CRUD ====================

    @Operation(summary = "获取参数列表")
    @PostMapping("/config")
    public AdminPageResult<Map<String, Object>> getConfigList(
            @RequestBody Map<String, Object> request) {
        int currentPage = getInt(request, "currentPage", 1);
        int pageSize = getInt(request, "pageSize", 10);
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<SysConfig> page = configService.findAll(pageable);
        List<Map<String, Object>> list =
                page.getContent().stream()
                        .filter(c -> !Boolean.TRUE.equals(c.getDeleted()))
                        .map(
                                c -> {
                                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                                    m.put("id", c.getConfigId());
                                    m.put("configName", c.getConfigName());
                                    m.put("configKey", c.getConfigKey());
                                    m.put("configValue", c.getConfigValue());
                                    m.put("configType", c.getConfigType());
                                    m.put("remark", c.getRemark());
                                    m.put("createTime", toEpochMilli(c.getCreateTime()));
                                    return m;
                                })
                        .collect(Collectors.toList());
        return AdminPageResult.of(list, page.getTotalElements(), pageSize, currentPage);
    }

    @Operation(summary = "创建参数")
    @PostMapping("/config/create")
    public Long createConfig(@RequestBody Map<String, Object> data) {
        SysConfig config = new SysConfig();
        config.setConfigName((String) data.get("configName"));
        config.setConfigKey((String) data.get("configKey"));
        config.setConfigValue((String) data.get("configValue"));
        config.setConfigType(
                data.get("configType") != null ? ((Number) data.get("configType")).intValue() : 0);
        config.setRemark((String) data.getOrDefault("remark", ""));
        SysConfig saved = configService.create(config);
        return saved.getConfigId();
    }

    @Operation(summary = "更新参数")
    @PutMapping("/config/update")
    public Boolean updateConfig(@RequestBody Map<String, Object> data) {
        Long id = ((Number) data.get("id")).longValue();
        Optional<SysConfig> opt = configService.findById(id);
        if (opt.isEmpty()) return false;
        SysConfig config = opt.get();
        if (data.containsKey("configName")) config.setConfigName((String) data.get("configName"));
        if (data.containsKey("configKey")) config.setConfigKey((String) data.get("configKey"));
        if (data.containsKey("configValue"))
            config.setConfigValue((String) data.get("configValue"));
        if (data.get("configType") != null)
            config.setConfigType(((Number) data.get("configType")).intValue());
        if (data.containsKey("remark")) config.setRemark((String) data.get("remark"));
        configService.update(config);
        return true;
    }

    @Operation(summary = "删除参数")
    @PostMapping("/config/delete")
    public Boolean deleteConfig(@RequestBody Map<String, Object> data) {
        Long id = ((Number) data.get("id")).longValue();
        configService.deleteById(id);
        return true;
    }

    // ==================== Notice CRUD ====================

    @Operation(summary = "获取通知公告列表")
    @PostMapping("/notice")
    public AdminPageResult<Map<String, Object>> getNoticeList(
            @RequestBody Map<String, Object> request) {
        int currentPage = getInt(request, "currentPage", 1);
        int pageSize = getInt(request, "pageSize", 10);
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<SysNotice> page = noticeService.findAll(pageable);
        List<Map<String, Object>> list =
                page.getContent().stream()
                        .filter(n -> !Boolean.TRUE.equals(n.getDeleted()))
                        .map(
                                n -> {
                                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                                    m.put("id", n.getNoticeId());
                                    m.put("noticeTitle", n.getNoticeTitle());
                                    m.put("noticeType", n.getNoticeType());
                                    m.put("noticeContent", n.getNoticeContent());
                                    m.put("status", n.getStatus());
                                    m.put("remark", n.getRemark());
                                    m.put("createTime", toEpochMilli(n.getCreateTime()));
                                    return m;
                                })
                        .collect(Collectors.toList());
        return AdminPageResult.of(list, page.getTotalElements(), pageSize, currentPage);
    }

    @Operation(summary = "创建通知公告")
    @PostMapping("/notice/create")
    public Long createNotice(@RequestBody Map<String, Object> data) {
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle((String) data.get("noticeTitle"));
        notice.setNoticeType(
                data.get("noticeType") != null ? ((Number) data.get("noticeType")).intValue() : 1);
        notice.setNoticeContent((String) data.getOrDefault("noticeContent", ""));
        notice.setStatus(data.get("status") != null ? ((Number) data.get("status")).intValue() : 1);
        notice.setRemark((String) data.getOrDefault("remark", ""));
        SysNotice saved = noticeService.create(notice);
        return saved.getNoticeId();
    }

    @Operation(summary = "更新通知公告")
    @PutMapping("/notice/update")
    public Boolean updateNotice(@RequestBody Map<String, Object> data) {
        Long id = ((Number) data.get("id")).longValue();
        Optional<SysNotice> opt = noticeService.findById(id);
        if (opt.isEmpty()) return false;
        SysNotice notice = opt.get();
        if (data.containsKey("noticeTitle"))
            notice.setNoticeTitle((String) data.get("noticeTitle"));
        if (data.get("noticeType") != null)
            notice.setNoticeType(((Number) data.get("noticeType")).intValue());
        if (data.containsKey("noticeContent"))
            notice.setNoticeContent((String) data.get("noticeContent"));
        if (data.get("status") != null) notice.setStatus(((Number) data.get("status")).intValue());
        if (data.containsKey("remark")) notice.setRemark((String) data.get("remark"));
        noticeService.update(notice);
        return true;
    }

    @Operation(summary = "删除通知公告")
    @PostMapping("/notice/delete")
    public Boolean deleteNotice(@RequestBody Map<String, Object> data) {
        Long id = ((Number) data.get("id")).longValue();
        noticeService.deleteById(id);
        return true;
    }

    // ==================== Operation Log ====================

    @Operation(summary = "获取操作日志列表")
    @PostMapping("/operation-logs")
    public AdminPageResult<Map<String, Object>> getOperationLogsList(
            @RequestBody Map<String, Object> request) {
        int currentPage = getInt(request, "currentPage", 1);
        int pageSize = getInt(request, "pageSize", 10);
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<SysOperLog> page = operLogService.findAll(pageable);
        List<Map<String, Object>> list =
                page.getContent().stream()
                        .filter(o -> !Boolean.TRUE.equals(o.getDeleted()))
                        .map(
                                o -> {
                                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                                    m.put("id", o.getOperId());
                                    m.put("username", o.getUsername());
                                    m.put("module", o.getModule());
                                    m.put("summary", o.getSummary());
                                    m.put("ip", o.getIp());
                                    m.put("address", o.getAddress());
                                    m.put("system", o.getSystemName());
                                    m.put("browser", o.getBrowser());
                                    m.put("status", o.getStatus());
                                    m.put("operatingTime", toEpochMilli(o.getOperatingTime()));
                                    return m;
                                })
                        .collect(Collectors.toList());
        return AdminPageResult.of(list, page.getTotalElements(), pageSize, currentPage);
    }

    @Operation(summary = "删除操作日志")
    @PostMapping("/operation-logs/delete")
    public Boolean deleteOperLog(@RequestBody Map<String, Object> data) {
        Long id = ((Number) data.get("id")).longValue();
        operLogService.deleteById(id);
        return true;
    }

    @Operation(summary = "清空操作日志")
    @PostMapping("/operation-logs/clear")
    public Boolean clearOperLogs() {
        operLogService.clearAll();
        return true;
    }

    // ==================== Login Log ====================

    @Operation(summary = "获取登录日志列表")
    @PostMapping("/login-logs")
    public AdminPageResult<Map<String, Object>> getLoginLogsList(
            @RequestBody Map<String, Object> request) {
        int currentPage = getInt(request, "currentPage", 1);
        int pageSize = getInt(request, "pageSize", 10);
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<SysLoginLog> page = loginLogService.findAll(pageable);
        List<Map<String, Object>> list =
                page.getContent().stream()
                        .filter(l -> !Boolean.TRUE.equals(l.getDeleted()))
                        .map(
                                l -> {
                                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                                    m.put("id", l.getInfoId());
                                    m.put("username", l.getUsername());
                                    m.put("ip", l.getIp());
                                    m.put("address", l.getAddress());
                                    m.put("system", l.getSystemName());
                                    m.put("browser", l.getBrowser());
                                    m.put("status", l.getStatus());
                                    m.put("behavior", l.getBehavior());
                                    m.put("loginTime", toEpochMilli(l.getLoginTime()));
                                    return m;
                                })
                        .collect(Collectors.toList());
        return AdminPageResult.of(list, page.getTotalElements(), pageSize, currentPage);
    }

    @Operation(summary = "删除登录日志")
    @PostMapping("/login-logs/delete")
    public Boolean deleteLoginLog(@RequestBody Map<String, Object> data) {
        Long id = ((Number) data.get("id")).longValue();
        loginLogService.deleteById(id);
        return true;
    }

    @Operation(summary = "清空登录日志")
    @PostMapping("/login-logs/clear")
    public Boolean clearLoginLogs() {
        loginLogService.clearAll();
        return true;
    }

    // ==================== Server Monitor ====================

    @Operation(summary = "获取服务器监控信息")
    @GetMapping("/server-info")
    public Map<String, Object> getServerInfo() {
        if (serverMonitorService == null) {
            return Collections.singletonMap("error", "服务器监控未启用");
        }
        return serverMonitorService.getServerInfo();
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

    // ==================== Menu Helper Methods ====================

    private SysMenu buildMenuFromData(Map<String, Object> data) {
        SysMenu menu = new SysMenu();
        updateMenuFromData(menu, data);
        return menu;
    }

    private void updateMenuFromData(SysMenu menu, Map<String, Object> data) {
        if (data.containsKey("parentId"))
            menu.setParentId(((Number) data.get("parentId")).longValue());
        if (data.containsKey("menuType"))
            menu.setMenuType(((Number) data.get("menuType")).intValue());
        if (data.containsKey("name")) menu.setRouterName((String) data.get("name"));
        if (data.containsKey("path")) menu.setPath((String) data.get("path"));
        if (data.containsKey("auths")) menu.setPermission((String) data.get("auths"));
        if (data.containsKey("status")) menu.setStatus(((Number) data.get("status")).intValue());
        MetaDTO meta = menu.getMetaInfo() != null ? menu.getMetaInfo() : new MetaDTO();
        if (data.containsKey("title")) meta.setTitle((String) data.get("title"));
        if (data.containsKey("icon")) meta.setIcon((String) data.get("icon"));
        if (data.get("rank") != null) meta.setRank(((Number) data.get("rank")).intValue());
        if (data.containsKey("showLink")) meta.setShowLink((Boolean) data.get("showLink"));
        if (data.containsKey("showParent")) meta.setShowParent((Boolean) data.get("showParent"));
        if (data.containsKey("keepAlive")) meta.setKeepAlive((Boolean) data.get("keepAlive"));
        if (data.containsKey("frameSrc")) meta.setFrameSrc((String) data.get("frameSrc"));
        if (data.containsKey("frameLoading"))
            meta.setFrameLoading((Boolean) data.get("frameLoading"));
        if (data.containsKey("hiddenTag")) meta.setHiddenTag((Boolean) data.get("hiddenTag"));
        menu.setMetaInfo(meta);
        if (data.containsKey("title")) menu.setMenuName((String) data.get("title"));
        if (menu.getStatus() == null) menu.setStatus(1);
    }

    private int getInt(Map<String, Object> map, String key, int defaultValue) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return defaultValue;
    }
}
