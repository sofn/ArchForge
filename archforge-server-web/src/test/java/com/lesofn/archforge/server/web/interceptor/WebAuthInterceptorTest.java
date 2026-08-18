package com.lesofn.archforge.server.web.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import com.lesofn.archforge.infrastructure.auth.stp.StpWebUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

class WebAuthInterceptorTest {

    private WebAuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new WebAuthInterceptor(JsonMapper.builder().build());
    }

    @Test
    void allowsPublicArticleListAndLogin() throws Exception {
        assertTrue(interceptor.preHandle(
                new MockHttpServletRequest("GET", "/web/articles"),
                new MockHttpServletResponse(),
                new Object()));
        assertTrue(interceptor.preHandle(
                new MockHttpServletRequest("POST", "/web/login"),
                new MockHttpServletResponse(),
                new Object()));
        assertTrue(interceptor.preHandle(
                new MockHttpServletRequest("OPTIONS", "/web/user/profile"),
                new MockHttpServletResponse(),
                new Object()));
    }

    @Test
    void rejectsProtectedPathWhenNotLoggedIn() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        try (MockedStatic<StpWebUtil> mocked = mockStatic(StpWebUtil.class)) {
            mocked.when(StpWebUtil::isLogin).thenReturn(false);

            boolean allowed = interceptor.preHandle(
                    new MockHttpServletRequest("GET", "/web/user/profile"), response, new Object());

            assertFalse(allowed);
            assertEquals(401, response.getStatus());
            assertTrue(response.getContentAsString().contains("未登录或 token 无效"));
        }
    }

    @Test
    void rejectsExpiredOrInvalidLoginState() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        try (MockedStatic<StpWebUtil> mocked = mockStatic(StpWebUtil.class)) {
            mocked.when(StpWebUtil::isLogin).thenReturn(false);

            boolean allowed = interceptor.preHandle(
                    new MockHttpServletRequest("GET", "/web/articles/me"), response, new Object());

            assertFalse(allowed);
            assertEquals(401, response.getStatus());
        }
    }
}
