package com.lesofn.archforge.infrastructure.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.infrastructure.auth.errors.AdminAuthException;
import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.auth.stp.LoginSessionKeys;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import com.lesofn.archforge.infrastructure.auth.stp.StpWebUtil;
import com.lesofn.archforge.infrastructure.user.web.RoleInfo;
import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginContextTest {

    @BeforeEach
    void setUp() {
        SaTokenContextMockUtil.setMockContext();
    }

    @AfterEach
    void tearDown() {
        StpAdminUtil.logout();
        StpWebUtil.logout();
        SaTokenContextMockUtil.clearContext();
    }

    @Test
    void getAdminUserReturnsSessionUser() {
        SystemLoginUser loginUser = new SystemLoginUser(1L, true, "admin", "secret", RoleInfo.EMPTY_ROLE, 4L);
        StpAdminUtil.login(1L);
        StpAdminUtil.getSession().set(LoginSessionKeys.LOGIN_USER, loginUser);

        assertEquals(loginUser, LoginContext.getAdminUser());
        assertEquals(1L, LoginContext.getAdminUserId());
        assertTrue(LoginContext.isAdmin());
    }

    @Test
    void getAdminUserThrowsWhenNotLoggedIn() {
        assertThrows(AdminAuthException.class, LoginContext::getAdminUser);
        assertTrue(LoginContext.findAdminUser().isEmpty());
    }

    @Test
    void getWebUserReadsWebSession() {
        StpWebUtil.login(7L);
        StpWebUtil.getSession().set(LoginSessionKeys.USERNAME, "alice");

        assertEquals(7L, LoginContext.getWebUserId());
        assertEquals("alice", LoginContext.getWebUsername());
    }
}
