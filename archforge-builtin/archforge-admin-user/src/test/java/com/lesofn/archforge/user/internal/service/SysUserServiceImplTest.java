package com.lesofn.archforge.user.internal.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.lesofn.archforge.user.api.dao.SysUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
}
