package com.lesofn.archforge.server.web.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.infrastructure.auth.LoginContext;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.api.service.SysUserService;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class MockWebAuthInterceptorTest {

    @Mock
    private SysUserService sysUserService;

    @AfterEach
    void tearDown() {
        LoginContext.clearWebUser();
    }

    @Test
    void setsContextFromExistingUser() {
        SysUser user = new SysUser();
        user.setUserId(7L);
        user.setUsername("alice");
        when(sysUserService.findById(7L)).thenReturn(Optional.of(user));
        MockWebAuthInterceptor interceptor = new MockWebAuthInterceptor(sysUserService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(MockWebAuthInterceptor.MOCK_USERID_HEADER, "7");

        interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertEquals(7L, LoginContext.getWebUserId());
        assertEquals("alice", LoginContext.getWebUsername());
    }

    @Test
    void usesFallbackNameWhenUserMissing() {
        when(sysUserService.findById(9L)).thenReturn(Optional.empty());
        MockWebAuthInterceptor interceptor = new MockWebAuthInterceptor(sysUserService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(MockWebAuthInterceptor.MOCK_USERID_HEADER, "9");

        interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertEquals(9L, LoginContext.getWebUserId());
        assertEquals("mock-user-9", LoginContext.getWebUsername());
    }

    @Test
    void ignoresInvalidHeader() {
        MockWebAuthInterceptor interceptor = new MockWebAuthInterceptor(sysUserService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(MockWebAuthInterceptor.MOCK_USERID_HEADER, "abc");

        interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertNull(LoginContext.getWebUserId());
    }
}
