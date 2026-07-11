package com.lesofn.archforge.server.admin.controller.admin;

import com.lesofn.archforge.common.enums.common.GenderEnum;
import com.lesofn.archforge.common.utils.query.QueryHelp;
import com.lesofn.archforge.infrastructure.annotation.Log;
import com.lesofn.archforge.server.admin.dto.AdminPageResult;
import com.lesofn.archforge.server.admin.dto.AdminUserItemDTO;
import com.lesofn.archforge.server.admin.dto.AdminUserListRequest;
import com.lesofn.archforge.server.admin.dto.request.UserCreateRequest;
import com.lesofn.archforge.server.admin.dto.request.UserDeleteRequest;
import com.lesofn.archforge.server.admin.dto.request.UserPasswordRequest;
import com.lesofn.archforge.server.admin.dto.request.UserRoleRequest;
import com.lesofn.archforge.server.admin.dto.request.UserStatusRequest;
import com.lesofn.archforge.server.admin.dto.request.UserUpdateRequest;
import com.lesofn.archforge.server.admin.dto.AdminUserIdRequest;
import com.lesofn.archforge.server.admin.dto.user.SysUserQueryCriteria;
import com.lesofn.archforge.server.admin.mapper.AdminUserMapper;
import com.lesofn.archforge.user.domain.SysDept;
import com.lesofn.archforge.user.domain.SysUser;
import com.lesofn.archforge.user.service.SysDeptService;
import com.lesofn.archforge.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class AdminUserController {

    private final SysUserService userService;
    private final AdminUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private SysDeptService deptService;

    @Operation(summary = "获取用户列表")
    @PostMapping("/user")
    public AdminPageResult<AdminUserItemDTO> getUserList(@RequestBody AdminUserListRequest request) {
        int currentPage = request.getCurrentPage() != null && request.getCurrentPage() > 0 ? request.getCurrentPage() : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0 ? request.getPageSize() : 10;

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

        Pageable pageable = PageRequest.of(currentPage - 1, pageSize,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "userId"));
        Specification<SysUser> spec = (root, q, cb) -> QueryHelp.getPredicate(root, criteria, cb);
        Page<SysUser> userPage = userService.findAll(spec, pageable);

        Map<Long, String> deptNameMap = buildDeptNameMap();
        List<AdminUserItemDTO> userItems = userPage.getContent().stream()
                .map(user -> userMapper.toDto(user, deptNameMap))
                .collect(Collectors.toList());
        return AdminPageResult.of(userItems, userPage.getTotalElements(), pageSize, currentPage);
    }

    @Operation(summary = "获取用户角色ID列表")
    @PostMapping("/list-role-ids")
    public List<Long> listRoleIds(@RequestBody AdminUserIdRequest request) {
        if (request.getUserId() == null) {
            return Collections.emptyList();
        }
        return userService.findById(request.getUserId())
                .map(user -> {
                    List<Long> roleIds = new ArrayList<>();
                    if (user.getRoleId() != null) {
                        roleIds.add(user.getRoleId());
                    }
                    return roleIds;
                })
                .orElse(Collections.emptyList());
    }

    @Log
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

    @Log
    @Operation(summary = "更新用户")
    @PutMapping("/user/update")
    public Boolean updateUser(@RequestBody @Valid UserUpdateRequest request) {
        Optional<SysUser> opt = userService.findById(request.getId());
        if (opt.isEmpty()) {
            return false;
        }
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

    @Log
    @Operation(summary = "删除用户")
    @PostMapping("/user/delete")
    public Boolean deleteUser(@RequestBody @Valid UserDeleteRequest request) {
        userService.softDeleteById(request.getId());
        return true;
    }

    @Log
    @Operation(summary = "更新用户状态")
    @PostMapping("/user/status")
    public Boolean updateUserStatus(@RequestBody @Valid UserStatusRequest request) {
        userService.updateStatus(request.getId(), request.getStatus());
        return true;
    }

    @Log
    @Operation(summary = "重置用户密码")
    @PostMapping("/user/reset-password")
    public Boolean resetUserPassword(@RequestBody @Valid UserPasswordRequest request) {
        String newPwd = request.getNewPwd() != null ? request.getNewPwd() : "admin123";
        userService.resetPassword(request.getId(), passwordEncoder.encode(newPwd));
        return true;
    }

    @Log
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

    private Map<Long, String> buildDeptNameMap() {
        if (deptService == null) {
            return Collections.emptyMap();
        }
        List<SysDept> allDepts = deptService.findAll();
        return allDepts.stream()
                .collect(Collectors.toMap(SysDept::getDeptId, SysDept::getName, (a, b) -> a));
    }
}
