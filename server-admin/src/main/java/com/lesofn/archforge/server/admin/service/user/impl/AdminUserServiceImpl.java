package com.lesofn.archforge.server.admin.service.user.impl;

import com.lesofn.archforge.common.enums.common.GenderEnum;
import com.lesofn.archforge.common.utils.query.QueryHelp;
import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.db.redis.RedisUtil;
import com.lesofn.archforge.infrastructure.security.datascope.DataPermission;
import com.lesofn.archforge.infrastructure.security.datascope.DataScopeContextHolder;
import com.lesofn.archforge.server.admin.datascope.DataScopeSpecification;
import com.lesofn.archforge.server.admin.dto.AdminPageResponse;
import com.lesofn.archforge.server.admin.dto.AdminUserDTO;
import com.lesofn.archforge.server.admin.dto.AdminUserListRequest;
import com.lesofn.archforge.server.admin.dto.CurrentLoginUserResponse;
import com.lesofn.archforge.server.admin.dto.UserResponse;
import com.lesofn.archforge.server.admin.dto.request.UserCreateRequest;
import com.lesofn.archforge.server.admin.dto.request.UserDeleteRequest;
import com.lesofn.archforge.server.admin.dto.request.UserPasswordRequest;
import com.lesofn.archforge.server.admin.dto.request.UserRoleRequest;
import com.lesofn.archforge.server.admin.dto.request.UserStatusRequest;
import com.lesofn.archforge.server.admin.dto.request.UserUpdateRequest;
import com.lesofn.archforge.server.admin.dto.user.SysUserQueryRequest;
import com.lesofn.archforge.server.admin.mapper.AdminUserConvertor;
import com.lesofn.archforge.server.admin.service.user.AdminUserService;
import com.lesofn.archforge.user.api.domain.SysDept;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.api.service.SysDeptService;
import com.lesofn.archforge.user.api.service.SysUserService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 *
 * @author lesofn
 */
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private static final String DEPT_NAME_MAP_KEY = "dept:nameMap";
    private static final int DEPT_NAME_MAP_TTL_MINUTES = 10;

    private final SysUserService sysUserService;
    private final SysDeptService sysDeptService;
    private final AdminUserConvertor adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;
    private final DataScopeSpecification dataScopeSpecification;

    @Override
    public CurrentLoginUserResponse getLoginUserInfo(SystemLoginUser loginUser) {
        CurrentLoginUserResponse currentUserResponse = new CurrentLoginUserResponse();

        // 根据用户ID查询用户完整信息
        SysUser user = sysUserService.findById(loginUser.getUserId()).orElse(null);

        // 创建UserResponse并设置用户信息
        UserResponse userDTO = new UserResponse(user);
        currentUserResponse.setUserInfo(userDTO);

        // 设置角色key
        if (loginUser.getRoleInfo() != null) {
            currentUserResponse.setRoleKey(loginUser.getRoleInfo().getRoleKey());
        } else {
            currentUserResponse.setRoleKey("");
        }

        // 设置权限列表
        if (loginUser.getRoleInfo() != null && loginUser.getRoleInfo().getMenuPermissions() != null) {
            currentUserResponse.setPermissions(loginUser.getRoleInfo().getMenuPermissions());
        } else {
            currentUserResponse.setPermissions(new HashSet<>());
        }

        return currentUserResponse;
    }

    @Override
    @DataPermission(deptAlias = "deptId", userAlias = "id")
    public AdminPageResponse<AdminUserDTO> getUserList(AdminUserListRequest request) {
        int currentPage = request.getCurrentPage() != null && request.getCurrentPage() > 0 ? request.getCurrentPage() : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0 ? request.getPageSize() : 10;

        SysUserQueryRequest criteria = new SysUserQueryRequest();
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

        Pageable pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Specification<SysUser> spec = (root, q, cb) -> QueryHelp.getPredicate(root, criteria, cb);
        spec = dataScopeSpecification.apply(spec, DataScopeContextHolder.get());
        Page<SysUser> userPage = sysUserService.findAll(spec, pageable);

        Map<Long, String> deptNameMap = getDeptNameMap();
        List<AdminUserDTO> userItems = userPage.getContent().stream()
                .map(user -> adminUserMapper.toDto(user, deptNameMap))
                .collect(Collectors.toList());
        return AdminPageResponse.of(userItems, userPage.getTotalElements(), pageSize, currentPage);
    }

    @Override
    public Long createUser(UserCreateRequest request) {
        SysUser user = adminUserMapper.fromCreateRequest(request);
        if (user.getPhoneNumber() == null) {
            user.setPhoneNumber("");
        }
        if (user.getEmail() == null) {
            user.setEmail("");
        }
        if (user.getRemark() == null) {
            user.setRemark("");
        }
        user.prepareForCreate(passwordEncoder.encode(request.getPassword()));
        SysUser saved = sysUserService.create(user);
        return saved.getUserId();
    }

    @Override
    public Boolean updateUser(UserUpdateRequest request) {
        Optional<SysUser> opt = sysUserService.findById(request.getId());
        if (opt.isEmpty()) {
            return false;
        }
        SysUser user = opt.get();
        user.updateProfile(
                request.getNickname(),
                request.getPhone(),
                request.getEmail(),
                request.getSex() != null ? GenderEnum.fromValue(request.getSex()) : null,
                request.getRemark(),
                request.getParentId());
        if (request.getStatus() != null) {
            user.updateStatus(request.getStatus());
        }
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        sysUserService.update(user);
        return true;
    }

    @Override
    public Boolean deleteUser(UserDeleteRequest request) {
        sysUserService.softDeleteById(request.getId());
        return true;
    }

    @Override
    public List<Long> getRoleIds(Long userId) {
        return sysUserService.findById(userId)
                .map(user -> {
                    List<Long> roleIds = new ArrayList<>();
                    if (user.getRoleId() != null) {
                        roleIds.add(user.getRoleId());
                    }
                    return roleIds;
                })
                .orElse(Collections.emptyList());
    }

    @Override
    public Boolean updateStatus(UserStatusRequest request) {
        sysUserService.updateStatus(request.getId(), request.getStatus());
        return true;
    }

    @Override
    public Boolean resetPassword(UserPasswordRequest request) {
        Optional<SysUser> opt = sysUserService.findById(request.getId());
        if (opt.isPresent()) {
            SysUser user = opt.get();
            user.changePassword(passwordEncoder.encode(request.getNewPwd()));
            sysUserService.update(user);
        }
        return true;
    }

    @Override
    public Boolean assignRole(UserRoleRequest request) {
        List<Long> ids = request.getIds();
        if (ids != null && !ids.isEmpty()) {
            Long roleId = ids.get(0);
            Optional<SysUser> opt = sysUserService.findById(request.getId());
            if (opt.isPresent()) {
                SysUser user = opt.get();
                user.assignRole(roleId);
                sysUserService.update(user);
            }
        }
        return true;
    }

    /** 构造部门名称映射，带 Redis TTL 缓存。 */
    private Map<Long, String> getDeptNameMap() {
        Map<String, String> cached = redisUtil.getCacheObject(DEPT_NAME_MAP_KEY);
        if (cached != null) {
            return cached.entrySet().stream()
                    .collect(Collectors.toMap(e -> Long.valueOf(e.getKey()), Map.Entry::getValue, (a, b) -> a));
        }

        Map<Long, String> map = sysDeptService.findAll().stream()
                .filter(dept -> dept.getDeptId() != null)
                .collect(Collectors.toMap(
                        SysDept::getDeptId,
                        dept -> dept.getName() != null ? dept.getName() : "",
                        (a, b) -> a));

        Map<String, String> toCache = map.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        Map.Entry::getValue,
                        (a, b) -> a));
        redisUtil.setCacheObject(DEPT_NAME_MAP_KEY, toCache, DEPT_NAME_MAP_TTL_MINUTES, TimeUnit.MINUTES);
        return map;
    }
}
