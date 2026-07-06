package com.lesofn.archforge.server.admin.controller;

import com.lesofn.archforge.common.enums.common.GenderEnum;
import com.lesofn.archforge.common.utils.query.QueryHelp;
import com.lesofn.archforge.server.admin.dto.*;
import com.lesofn.archforge.server.admin.dto.request.*;
import com.lesofn.archforge.server.admin.dto.user.SysUserQueryCriteria;
import com.lesofn.archforge.server.admin.mapper.AdminDeptMapper;
import com.lesofn.archforge.server.admin.mapper.AdminMenuMapper;
import com.lesofn.archforge.server.admin.mapper.AdminRoleMapper;
import com.lesofn.archforge.server.admin.mapper.AdminRoleMenuMapper;
import com.lesofn.archforge.server.admin.mapper.AdminUserMapper;
import com.lesofn.archforge.server.admin.service.monitor.ServerMonitorService;
import com.lesofn.archforge.user.dao.SysRoleMenuRepository;
import com.lesofn.archforge.user.domain.SysConfig;
import com.lesofn.archforge.user.domain.SysDept;
import com.lesofn.archforge.user.domain.SysLoginLog;
import com.lesofn.archforge.user.domain.SysMenu;
import com.lesofn.archforge.user.domain.SysNotice;
import com.lesofn.archforge.user.domain.SysOperLog;
import com.lesofn.archforge.user.domain.SysRole;
import com.lesofn.archforge.user.domain.SysRoleMenu;
import com.lesofn.archforge.user.domain.SysUser;
import com.lesofn.archforge.user.menu.SysMenuService;
import com.lesofn.archforge.user.menu.dto.MetaDTO;
import com.lesofn.archforge.user.service.SysConfigService;
import com.lesofn.archforge.user.service.SysDeptService;
import com.lesofn.archforge.user.service.SysLoginLogService;
import com.lesofn.archforge.user.service.SysNoticeService;
import com.lesofn.archforge.user.service.SysOperLogService;
import com.lesofn.archforge.user.service.SysRoleMenuService;
import com.lesofn.archforge.user.service.SysRoleService;
import com.lesofn.archforge.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.data.jpa.domain.Specification;
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
    private final AdminUserMapper userMapper;
    private final AdminRoleMapper roleMapper;
    private final AdminMenuMapper menuMapper;
    private final AdminRoleMenuMapper roleMenuMapper;
    private final AdminDeptMapper deptMapper;

    @Autowired(required = false)
    private SysDeptService deptService;

    @Autowired(required = false)
    private ServerMonitorService serverMonitorService;

    // ==================== 列表查询 ====================

    @Operation(summary = "获取用户列表")
    @PostMapping("/user")
    public AdminPageResult<AdminUserItemDTO> getUserList(
            @RequestBody AdminUserListRequest request) {
        int currentPage = request.getCurrentPage() != null && request.getCurrentPage() > 0
                ? request.getCurrentPage()
                : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0
                ? request.getPageSize()
                : 10;

        SysUserQueryCriteria criteria = new SysUserQueryCriteria();
        criteria.setBlurry(request.getBlurry());
        criteria.setUsername(request.getUsername());
        criteria.setEmail(request.getEmail());
        criteria.setPhoneNumber(request.getPhone());
        criteria.setStatus(request.getStatusAsInt());
        criteria.setDeptId(request.getDeptIdAsLong());
        criteria.setDeleted(false);
        if (request.getCreateTime() != null && request.getCreateTime().size() == 2) {
            criteria.setCreateTime(request.getCreateTime());
        }

        Pageable pageable = PageRequest.of(
                currentPage - 1,
                pageSize,
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "userId"));
        Specification<SysUser> spec = (root, q, cb) -> QueryHelp.getPredicate(root, criteria, cb);
        Page<SysUser> userPage = userService.findAll(spec, pageable);

        Map<Long, String> deptNameMap = buildDeptNameMap();
        List<AdminUserItemDTO> userItems = userPage.getContent().stream()
                .map(user -> userMapper.toDto(user, deptNameMap))
                .collect(Collectors.toList());
        return AdminPageResult.of(userItems, userPage.getTotalElements(), pageSize, currentPage);
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
        int currentPage = request.getCurrentPage() != null && request.getCurrentPage() > 0
                ? request.getCurrentPage()
                : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0
                ? request.getPageSize()
                : 10;

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
        List<SysMenu> allMenus = menuService.findAllActiveMenus();
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

    @Operation(summary = "获取全量菜单列表")
    @PostMapping("/menu")
    public List<AdminMenuItemDTO> getMenuList() {
        List<SysMenu> allMenus = menuService.findAllActiveMenus();
        return allMenus.stream().map(menuMapper::toDto).collect(Collectors.toList());
    }

    @Operation(summary = "获取全量部门列表")
    @PostMapping("/dept")
    public List<AdminDeptItemDTO> getDeptList() {
        if (deptService == null) {
            return Collections.emptyList();
        }
        List<SysDept> allDepts = deptService.findAll();
        return allDepts.stream().map(deptMapper::toDto).collect(Collectors.toList());
    }

    // ==================== User CRUD ====================

    @Operation(summary = "创建用户")
    @PostMapping("/user/create")
    public Long createUser(@RequestBody @Valid UserCreateRequest request) {
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setPhoneNumber(request.getPhone() != null ? request.getPhone() : "");
        user.setEmail(request.getEmail() != null ? request.getEmail() : "");
        if (request.getSex() != null) {
            user.setSex(GenderEnum.fromValue(request.getSex()));
        }
        user.setStatus(request.getStatus());
        user.setRemark(request.getRemark() != null ? request.getRemark() : "");
        if (request.getParentId() != null) {
            user.setDeptId(request.getParentId());
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        SysUser saved = userService.create(user);
        return saved.getUserId();
    }

    @Operation(summary = "更新用户")
    @PutMapping("/user/update")
    public Boolean updateUser(@RequestBody @Valid UserUpdateRequest request) {
        Optional<SysUser> opt = userService.findById(request.getId());
        if (opt.isEmpty())
            return false;
        SysUser user = opt.get();
        if (request.getUsername() != null)
            user.setUsername(request.getUsername());
        if (request.getNickname() != null)
            user.setNickname(request.getNickname());
        if (request.getPhone() != null)
            user.setPhoneNumber(request.getPhone());
        if (request.getEmail() != null)
            user.setEmail(request.getEmail());
        if (request.getSex() != null)
            user.setSex(GenderEnum.fromValue(request.getSex()));
        if (request.getStatus() != null)
            user.setStatus(request.getStatus());
        if (request.getRemark() != null)
            user.setRemark(request.getRemark());
        if (request.getParentId() != null)
            user.setDeptId(request.getParentId());
        userService.update(user);
        return true;
    }

    @Operation(summary = "删除用户")
    @PostMapping("/user/delete")
    public Boolean deleteUser(@RequestBody @Valid UserDeleteRequest request) {
        userService.softDeleteById(request.getId());
        return true;
    }

    @Operation(summary = "更新用户状态")
    @PostMapping("/user/status")
    public Boolean updateUserStatus(@RequestBody @Valid UserStatusRequest request) {
        userService.updateStatus(request.getId(), request.getStatus());
        return true;
    }

    @Operation(summary = "重置用户密码")
    @PostMapping("/user/reset-password")
    public Boolean resetUserPassword(@RequestBody @Valid UserPasswordRequest request) {
        String newPwd = request.getNewPwd() != null ? request.getNewPwd() : "admin123";
        userService.resetPassword(request.getId(), passwordEncoder.encode(newPwd));
        return true;
    }

    @Operation(summary = "分配用户角色")
    @PostMapping("/user/assign-role")
    public Boolean assignUserRole(@RequestBody @Valid UserRoleRequest request) {
        List<Long> ids = request.getIds();
        if (ids != null && !ids.isEmpty()) {
            Long roleId = ids.get(0);
            Optional<SysUser> opt = userService.findById(request.getId());
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
        if (opt.isEmpty())
            return false;
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

    // ==================== Menu CRUD ====================

    @Operation(summary = "创建菜单")
    @PostMapping("/menu/create")
    public Long createMenu(@RequestBody @Valid MenuCreateRequest request) {
        SysMenu menu = buildMenuFromCreateRequest(request);
        SysMenu saved = menuService.create(menu);
        return saved.getMenuId();
    }

    @Operation(summary = "更新菜单")
    @PutMapping("/menu/update")
    public Boolean updateMenu(@RequestBody @Valid MenuUpdateRequest request) {
        Optional<SysMenu> opt = menuService.findById(request.getId());
        if (opt.isEmpty())
            return false;
        SysMenu menu = opt.get();
        applyMenuFields(
                menu,
                request.getParentId(),
                request.getMenuType(),
                request.getName(),
                request.getPath(),
                request.getAuths(),
                request.getStatus(),
                request.getTitle(),
                request.getIcon(),
                request.getRank(),
                request.getShowLink(),
                request.getShowParent(),
                request.getKeepAlive(),
                request.getFrameSrc(),
                request.getFrameLoading(),
                request.getHiddenTag());
        menu.setStatus(menu.getStatus() != null ? menu.getStatus() : 1);
        menuService.update(menu);
        return true;
    }

    @Operation(summary = "删除菜单")
    @PostMapping("/menu/delete")
    public Boolean deleteMenu(@RequestBody @Valid MenuDeleteRequest request) {
        menuService.softDeleteById(request.getId());
        return true;
    }

    // ==================== Dept CRUD ====================

    @Operation(summary = "创建部门")
    @PostMapping("/dept/create")
    public Long createDept(@RequestBody @Valid DeptCreateRequest request) {
        SysDept dept = new SysDept();
        dept.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        dept.setName(request.getName());
        dept.setPrincipal(request.getPrincipal() != null ? request.getPrincipal() : "");
        dept.setPhone(request.getPhone() != null ? request.getPhone() : "");
        dept.setEmail(request.getEmail() != null ? request.getEmail() : "");
        dept.setSort(request.getSort() != null ? request.getSort() : 0);
        dept.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        dept.setRemark(request.getRemark() != null ? request.getRemark() : "");
        SysDept saved = deptService.create(dept);
        return saved.getDeptId();
    }

    @Operation(summary = "更新部门")
    @PutMapping("/dept/update")
    public Boolean updateDept(@RequestBody @Valid DeptUpdateRequest request) {
        Optional<SysDept> opt = deptService.findById(request.getId());
        if (opt.isEmpty())
            return false;
        SysDept dept = opt.get();
        if (request.getName() != null)
            dept.setName(request.getName());
        if (request.getPrincipal() != null)
            dept.setPrincipal(request.getPrincipal());
        if (request.getPhone() != null)
            dept.setPhone(request.getPhone());
        if (request.getEmail() != null)
            dept.setEmail(request.getEmail());
        if (request.getSort() != null)
            dept.setSort(request.getSort());
        if (request.getStatus() != null)
            dept.setStatus(request.getStatus());
        if (request.getRemark() != null)
            dept.setRemark(request.getRemark());
        if (request.getParentId() != null)
            dept.setParentId(request.getParentId());
        deptService.update(dept);
        return true;
    }

    @Operation(summary = "删除部门")
    @PostMapping("/dept/delete")
    public Boolean deleteDept(@RequestBody @Valid DeptDeleteRequest request) {
        deptService.deleteById(request.getId());
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
        List<Map<String, Object>> list = page.getContent().stream()
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
    public Long createConfig(@RequestBody @Valid ConfigCreateRequest request) {
        SysConfig config = new SysConfig();
        config.setConfigName(request.getConfigName());
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setConfigType(request.getConfigType() != null ? request.getConfigType() : 0);
        config.setRemark(request.getRemark() != null ? request.getRemark() : "");
        SysConfig saved = configService.create(config);
        return saved.getConfigId();
    }

    @Operation(summary = "更新参数")
    @PutMapping("/config/update")
    public Boolean updateConfig(@RequestBody @Valid ConfigUpdateRequest request) {
        Optional<SysConfig> opt = configService.findById(request.getId());
        if (opt.isEmpty())
            return false;
        SysConfig config = opt.get();
        if (request.getConfigName() != null)
            config.setConfigName(request.getConfigName());
        if (request.getConfigKey() != null)
            config.setConfigKey(request.getConfigKey());
        if (request.getConfigValue() != null)
            config.setConfigValue(request.getConfigValue());
        if (request.getConfigType() != null)
            config.setConfigType(request.getConfigType());
        if (request.getRemark() != null)
            config.setRemark(request.getRemark());
        configService.update(config);
        return true;
    }

    @Operation(summary = "删除参数")
    @PostMapping("/config/delete")
    public Boolean deleteConfig(@RequestBody @Valid ConfigDeleteRequest request) {
        configService.deleteById(request.getId());
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
        List<Map<String, Object>> list = page.getContent().stream()
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
    public Long createNotice(@RequestBody @Valid NoticeCreateRequest request) {
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle(request.getNoticeTitle());
        notice.setNoticeType(request.getNoticeType() != null ? request.getNoticeType() : 1);
        notice.setNoticeContent(
                request.getNoticeContent() != null ? request.getNoticeContent() : "");
        notice.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        notice.setRemark(request.getRemark() != null ? request.getRemark() : "");
        SysNotice saved = noticeService.create(notice);
        return saved.getNoticeId();
    }

    @Operation(summary = "更新通知公告")
    @PutMapping("/notice/update")
    public Boolean updateNotice(@RequestBody @Valid NoticeUpdateRequest request) {
        Optional<SysNotice> opt = noticeService.findById(request.getId());
        if (opt.isEmpty())
            return false;
        SysNotice notice = opt.get();
        if (request.getNoticeTitle() != null)
            notice.setNoticeTitle(request.getNoticeTitle());
        if (request.getNoticeType() != null)
            notice.setNoticeType(request.getNoticeType());
        if (request.getNoticeContent() != null)
            notice.setNoticeContent(request.getNoticeContent());
        if (request.getStatus() != null)
            notice.setStatus(request.getStatus());
        if (request.getRemark() != null)
            notice.setRemark(request.getRemark());
        noticeService.update(notice);
        return true;
    }

    @Operation(summary = "删除通知公告")
    @PostMapping("/notice/delete")
    public Boolean deleteNotice(@RequestBody @Valid NoticeDeleteRequest request) {
        noticeService.deleteById(request.getId());
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
        List<Map<String, Object>> list = page.getContent().stream()
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
        List<Map<String, Object>> list = page.getContent().stream()
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

    // ==================== 私有辅助方法 ====================

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

    private SysMenu buildMenuFromCreateRequest(MenuCreateRequest request) {
        SysMenu menu = new SysMenu();
        applyMenuFields(
                menu,
                request.getParentId(),
                request.getMenuType(),
                request.getName(),
                request.getPath(),
                request.getAuths(),
                request.getStatus(),
                request.getTitle(),
                request.getIcon(),
                request.getRank(),
                request.getShowLink(),
                request.getShowParent(),
                request.getKeepAlive(),
                request.getFrameSrc(),
                request.getFrameLoading(),
                request.getHiddenTag());
        menu.setStatus(menu.getStatus() != null ? menu.getStatus() : 1);
        return menu;
    }

    private void applyMenuFields(
            SysMenu menu,
            Long parentId,
            Integer menuType,
            String name,
            String path,
            String auths,
            Integer status,
            String title,
            String icon,
            Integer rank,
            Boolean showLink,
            Boolean showParent,
            Boolean keepAlive,
            String frameSrc,
            Boolean frameLoading,
            Boolean hiddenTag) {
        if (parentId != null)
            menu.setParentId(parentId);
        if (menuType != null)
            menu.setMenuType(menuType);
        if (name != null)
            menu.setRouterName(name);
        if (path != null)
            menu.setPath(path);
        if (auths != null)
            menu.setPermission(auths);
        if (status != null)
            menu.setStatus(status);
        MetaDTO meta = menu.getMetaInfo() != null ? menu.getMetaInfo() : new MetaDTO();
        if (title != null)
            meta.setTitle(title);
        if (icon != null)
            meta.setIcon(icon);
        if (rank != null)
            meta.setRank(rank);
        if (showLink != null)
            meta.setShowLink(showLink);
        if (showParent != null)
            meta.setShowParent(showParent);
        if (keepAlive != null)
            meta.setKeepAlive(keepAlive);
        if (frameSrc != null)
            meta.setFrameSrc(frameSrc);
        if (frameLoading != null)
            meta.setFrameLoading(frameLoading);
        if (hiddenTag != null)
            meta.setHiddenTag(hiddenTag);
        menu.setMetaInfo(meta);
        if (title != null)
            menu.setMenuName(title);
    }

    private int getInt(Map<String, Object> map, String key, int defaultValue) {
        return Optional.ofNullable(map.get(key))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(Number::intValue)
                .orElse(defaultValue);
    }
}
