package com.lesofn.archforge.server.admin.service.login;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.user.web.RoleInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class AdminUserDetailsServiceTest {

    @Mock
    private AdminLoginUserFactory adminLoginUserFactory;

    @InjectMocks
    private AdminUserDetailsService adminUserDetailsService;

    @Test
    void loadUserByUsernameDelegatesToFactory() {
        SystemLoginUser loginUser = new SystemLoginUser(1L, true, "admin", "secret", RoleInfo.EMPTY_ROLE, 4L);
        when(adminLoginUserFactory.load("admin")).thenReturn(loginUser);

        UserDetails result = adminUserDetailsService.loadUserByUsername("admin");

        assertSame(loginUser, result);
        verify(adminLoginUserFactory).load("admin");
    }

    @Test
    void getRoleInfoDelegatesToFactory() {
        when(adminLoginUserFactory.getRoleInfo(2L, false)).thenReturn(RoleInfo.EMPTY_ROLE);

        assertSame(RoleInfo.EMPTY_ROLE, adminUserDetailsService.getRoleInfo(2L, false));
        verify(adminLoginUserFactory).getRoleInfo(2L, false);
    }
}
