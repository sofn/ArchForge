package com.lesofn.archforge.server.admin.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.user.web.RoleInfo;
import com.lesofn.archforge.infrastructure.db.redis.RedisUtil;
import com.lesofn.archforge.server.admin.dto.CurrentLoginUserResponse;
import com.lesofn.archforge.server.admin.dto.UserResponse;
import com.lesofn.archforge.server.admin.dto.request.UserCreateRequest;
import com.lesofn.archforge.server.admin.dto.request.UserDeleteRequest;
import com.lesofn.archforge.server.admin.dto.request.UserPasswordRequest;
import com.lesofn.archforge.server.admin.dto.request.UserRoleRequest;
import com.lesofn.archforge.server.admin.dto.request.UserStatusRequest;
import com.lesofn.archforge.server.admin.dto.request.UserUpdateRequest;
import com.lesofn.archforge.server.admin.mapper.AdminUserConvertor;
import com.lesofn.archforge.server.admin.datascope.DataScopeSpecification;
import com.lesofn.archforge.server.admin.service.user.impl.AdminUserServiceImpl;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.api.service.SysDeptService;
import com.lesofn.archforge.user.api.service.SysUserService;
import com.lesofn.archforge.user.testing.RoleTestBuilder;
import com.lesofn.archforge.user.testing.UserTestBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Unit tests for {@link AdminUserServiceImpl} (user CRUD business rules). */
@Tag("P0")
@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private SysUserService sysUserService;
    @Mock
    private SysDeptService sysDeptService;
    @Mock
    private AdminUserConvertor adminUserMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RedisUtil redisUtil;
    @Mock
    private DataScopeSpecification dataScopeSpecification;

    @InjectMocks
    private AdminUserServiceImpl service;

    // ===================== createUser =====================

    @Test
    void createUserEncodesPasswordAndDefaultsBlankContactFields() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newbie");
        request.setPassword("raw-password");
        when(adminUserMapper.fromCreateRequest(request)).thenReturn(new SysUser());
        when(passwordEncoder.encode("raw-password")).thenReturn("{bcrypt}encoded");
        SysUser saved = UserTestBuilder.aUser().withUserId(42L).build();
        when(sysUserService.create(any(SysUser.class))).thenReturn(saved);

        Long userId = service.createUser(request);

        assertEquals(42L, userId);
        verify(sysUserService)
                .create(
                        org.mockito.ArgumentMatchers.argThat(
                                user -> {
                                    assertNotNull(user);
                                    assertEquals("", user.getPhoneNumber());
                                    assertEquals("", user.getEmail());
                                    assertEquals("", user.getRemark());
                                    assertEquals("{bcrypt}encoded", user.getPassword());
                                    assertTrue(user.canLogin(), "prepareForCreate must activate the user");
                                    assertFalse(user.getIsAdmin());
                                    return true;
                                }));
    }

    // ===================== updateUser =====================

    @Test
    void updateUserReturnsFalseWhenUserMissing() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setId(7L);
        when(sysUserService.findById(7L)).thenReturn(Optional.empty());

        assertFalse(service.updateUser(request));
        verify(sysUserService, never()).updateProfile(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void updateUserProfilesAndAppliesOptionalStatus() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setId(7L);
        request.setStatus(2);
        when(sysUserService.findById(7L)).thenReturn(Optional.of(UserTestBuilder.aUser().build()));

        assertTrue(service.updateUser(request));
        verify(sysUserService).updateProfile(7L, null, null, null, null, null, null, null);
        verify(sysUserService).updateStatus(7L, 2);
    }

    // ===================== deleteUser / updateStatus =====================

    @Test
    void deleteUserSoftDeletes() {
        UserDeleteRequest request = new UserDeleteRequest();
        request.setId(9L);

        assertTrue(service.deleteUser(request));
        verify(sysUserService).softDeleteById(9L);
    }

    @Test
    void updateStatusDelegatesToDomainService() {
        UserStatusRequest request = new UserStatusRequest();
        request.setId(9L);
        request.setStatus(1);

        assertTrue(service.updateStatus(request));
        verify(sysUserService).updateStatus(9L, 1);
    }

    // ===================== resetPassword =====================

    @Test
    void resetPasswordEncodesBeforeDelegating() {
        UserPasswordRequest request = new UserPasswordRequest();
        request.setId(3L);
        request.setNewPwd("brand-new");
        when(passwordEncoder.encode("brand-new")).thenReturn("{bcrypt}new");

        assertTrue(service.resetPassword(request));
        verify(sysUserService).resetPassword(3L, "{bcrypt}new");
    }

    // ===================== assignRole =====================

    @Test
    void assignRoleUsesFirstRoleIdFromList() {
        UserRoleRequest request = new UserRoleRequest();
        request.setId(5L);
        request.setIds(List.of(11L, 12L));

        assertTrue(service.assignRole(request));
        verify(sysUserService).assignRole(5L, 11L);
    }

    @Test
    void assignRoleWithoutIdsIsNoOp() {
        UserRoleRequest request = new UserRoleRequest();
        request.setId(5L);

        assertTrue(service.assignRole(request));
        verify(sysUserService, never()).assignRole(any(), any());
    }

    // ===================== getRoleIds =====================

    @Test
    void getRoleIdsReturnsSingletonListForAssignedUser() {
        SysUser user = UserTestBuilder.aUser().withRoleId(77L).build();
        when(sysUserService.findById(5L)).thenReturn(Optional.of(user));

        assertEquals(List.of(77L), service.getRoleIds(5L));
    }

    @Test
    void getRoleIdsReturnsEmptyListForUnknownUser() {
        when(sysUserService.findById(404L)).thenReturn(Optional.empty());

        assertTrue(service.getRoleIds(404L).isEmpty());
    }

    // ===================== getLoginUserInfo =====================

    @Test
    void loginUserInfoMapsRoleKeyAndPermissions() {
        RoleInfo roleInfo = new RoleInfo(77L, "ROLE_ADMIN", null, null, Set.of("user:list", "user:create"), null);
        SystemLoginUser loginUser = new SystemLoginUser(1L, false, "admin", "pwd", roleInfo, null);
        SysUser user = UserTestBuilder.aUser().build();
        when(sysUserService.findById(1L)).thenReturn(Optional.of(user));

        CurrentLoginUserResponse response = service.getLoginUserInfo(loginUser);

        assertEquals("ROLE_ADMIN", response.getRoleKey());
        assertTrue(response.getPermissions().contains("user:list"));
        assertNotNull(response.getUserInfo());
        assertEquals(user.getUserId(), response.getUserInfo().getUserId());
    }

    @Test
    void loginUserInfoFallsBackToEmptyRoleAndPermissions() {
        SystemLoginUser loginUser = new SystemLoginUser(2L, false, "u", "p", null, null);
        when(sysUserService.findById(2L)).thenReturn(Optional.of(UserTestBuilder.aUser().build()));

        CurrentLoginUserResponse response = service.getLoginUserInfo(loginUser);

        assertEquals("", response.getRoleKey());
        assertTrue(response.getPermissions().isEmpty());
    }
}
