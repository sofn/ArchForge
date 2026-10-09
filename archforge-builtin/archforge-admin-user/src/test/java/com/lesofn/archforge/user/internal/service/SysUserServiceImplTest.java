package com.lesofn.archforge.user.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.common.enums.common.UserStatusEnum;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.internal.dao.SysUserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SysUserServiceImplTest {

    @Mock
    private SysUserRepository userRepository;

    @InjectMocks
    private SysUserServiceImpl sysUserService;

    @Test
    void findByUsernameAllowsEmailFallback() {
        Optional<?> user = sysUserService.findByUsername("alice@example.com");

        assertTrue(user.isEmpty());
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void lookupsRejectMalformedKeysWithoutQuerying() {
        assertTrue(sysUserService.findById(0L).isEmpty());
        assertTrue(sysUserService.findByEmail("not-an-email").isEmpty());
        assertTrue(sysUserService.findByPhoneNumber(" ").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> sysUserService.findByPhoneNumber("12345"));
        assertFalse(sysUserService.existsByUsername("x"));
        assertFalse(sysUserService.existsByEmail(""));
        assertThrows(IllegalArgumentException.class, () -> sysUserService.existsByEmail("bad"));
        assertThrows(IllegalArgumentException.class, () -> sysUserService.existsByPhoneNumber("999"));
        verifyNoInteractions(userRepository);
    }

    @Test
    void validLookupsGoToTheRepository() {
        SysUser alice = user(1L, "alice");
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername("alice")).thenReturn(alice);
        when(userRepository.findByEmail("a@b.co")).thenReturn(alice);
        when(userRepository.findByPhoneNumber("13800138000")).thenReturn(alice);
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        when(userRepository.existsByEmail("a@b.co")).thenReturn(true);
        when(userRepository.existsByPhoneNumber("13800138000")).thenReturn(true);

        assertSame(alice, sysUserService.findById(1L).orElseThrow());
        assertSame(alice, sysUserService.getUserByUserName("alice"));
        assertSame(alice, sysUserService.findByEmail("a@b.co").orElseThrow());
        assertSame(alice, sysUserService.findByPhoneNumber("13800138000").orElseThrow());
        assertTrue(sysUserService.existsByUsername("alice"));
        assertTrue(sysUserService.existsByEmail("a@b.co"));
        assertTrue(sysUserService.existsByPhoneNumber("13800138000"));
    }

    @Test
    void createRejectsInvalidUsers() {
        assertThrows(IllegalArgumentException.class, () -> sysUserService.create(newUser("!")));
        SysUser noPassword = newUser("bob");
        noPassword.setPassword(" ");
        assertThrows(IllegalArgumentException.class, () -> sysUserService.create(noPassword));
        SysUser badEmail = newUser("bob");
        badEmail.setEmail("nope");
        assertThrows(IllegalArgumentException.class, () -> sysUserService.create(badEmail));
        SysUser badPhone = newUser("bob");
        badPhone.setPhoneNumber("123");
        assertThrows(IllegalArgumentException.class, () -> sysUserService.create(badPhone));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createFillsDefaults() {
        when(userRepository.save(any(SysUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        SysUser bob = new SysUser();
        bob.setUsername("bob");
        bob.setPassword("hash");

        SysUser saved = sysUserService.create(bob);

        assertEquals(UserStatusEnum.NORMAL.getValue(), saved.getStatus());
        assertEquals(0L, saved.getRoleId());
    }

    @Test
    void updateKeepsFieldsTheCallerLeftBlank() {
        SysUser existing = user(5L, "carol");
        existing.setNickname("Carol");
        existing.setAvatar("a.png");
        existing.setRemark("keep me");
        when(userRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(SysUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        SysUser incoming = user(5L, "carol");
        incoming.setNickname(" ");

        SysUser saved = sysUserService.update(incoming);

        assertEquals("Carol", saved.getNickname());
        assertEquals("a.png", saved.getAvatar());
        assertEquals("keep me", saved.getRemark());
    }

    @Test
    void stateChangesApplyToExistingUsersOnly() {
        SysUser dave = user(7L, "dave");
        dave.setRoleId(1L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(dave));

        sysUserService.updateStatus(7L, UserStatusEnum.NORMAL.getValue());
        sysUserService.updatePassword(7L, "hash-2");
        sysUserService.updateLoginInfo(7L, "10.0.0.9");
        sysUserService.assignRole(7L, 3L);
        sysUserService.softDeleteById(7L);
        sysUserService.updateStatus(-1L, 2);

        assertEquals(UserStatusEnum.NORMAL.getValue(), dave.getStatus());
        assertEquals("hash-2", dave.getPassword());
        assertEquals("10.0.0.9", dave.getLoginIp());
        assertEquals(3L, dave.getRoleId());
        assertTrue(dave.isDeleted());
    }

    @Test
    void updateProfileValidatesWhatItChanges() {
        SysUser erin = user(9L, "erin");
        when(userRepository.findById(9L)).thenReturn(Optional.of(erin));

        assertThrows(IllegalArgumentException.class,
                () -> sysUserService.updateProfile(9L, "?", null, null, null, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> sysUserService.updateProfile(9L, null, null, "1", null, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> sysUserService.updateProfile(9L, null, null, null, "x", null, null, null));
        sysUserService.updateProfile(9L, "erin2", "Erin", "13900139000", "e@f.io", null, "hi", 4L);

        assertEquals("erin2", erin.getUsername());
        assertEquals("Erin", erin.getNickname());
        assertEquals("13900139000", erin.getPhoneNumber());
        assertEquals(4L, erin.getDeptId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void countsAndListingsDelegate() {
        when(userRepository.countByDeletedFalse()).thenReturn(12L);
        when(userRepository.countByDeletedFalseAndStatus(1)).thenReturn(10L);
        when(userRepository.findAll(any(Specification.class))).thenReturn(List.of(user(1L, "a")));

        assertEquals(12L, sysUserService.countNotDeleted());
        assertEquals(10L, sysUserService.countNotDeletedWithStatus(1));
        assertEquals(1, sysUserService.findActiveUsers().size());
        assertEquals(1, sysUserService.findByDeptId(2L).size());
        assertNull(sysUserService.getUserByUserName("??"));
    }

    private static SysUser user(Long id, String username) {
        SysUser user = newUser(username);
        user.setUserId(id);
        return user;
    }

    private static SysUser newUser(String username) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword("hash");
        user.setStatus(UserStatusEnum.NORMAL.getValue());
        user.setRoleId(0L);
        return user;
    }
}
