package com.lesofn.archforge.user.internal.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.user.domain.adapter.repository.UserRepository;
import com.lesofn.archforge.user.domain.valueobject.Username;
import com.lesofn.archforge.user.internal.convert.SysUserConvertor;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SysUserConvertor sysUserConvertor;

    @InjectMocks
    private SysUserServiceImpl sysUserService;

    @Test
    void findByUsernameAllowsEmailFallback() {
        Optional<?> user = sysUserService.findByUsername("alice@example.com");

        assertTrue(user.isEmpty());
        verify(userRepository, never()).findByUsername(any(Username.class));
    }
}
