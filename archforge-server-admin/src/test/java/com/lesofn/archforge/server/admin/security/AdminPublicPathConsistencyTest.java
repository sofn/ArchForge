package com.lesofn.archforge.server.admin.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.lesofn.archforge.server.admin.config.AdminSaTokenConfig;
import com.lesofn.archforge.server.admin.controller.auth.LoginController;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * A path on the login-free list with no endpoint behind it is a pre-opened slot: whoever adds an
 * endpoint there later ships it unauthenticated without noticing ({@code /admin/auth/register} was
 * exactly that). Every public {@code /admin/auth/**} path must be a real {@link LoginController} endpoint.
 */
@Tag("contract")
class AdminPublicPathConsistencyTest {

    @Test
    void everyPublicAuthPathIsARealLoginControllerEndpoint() {
        Set<String> real = loginControllerPaths();
        List<String> orphans = new ArrayList<>();
        for (String path : AdminSaTokenConfig.PUBLIC_PATHS) {
            if (path.startsWith("/admin/auth/") && !real.contains(path)) {
                orphans.add(path);
            }
        }

        assertEquals(List.of(), orphans, "login-free paths without a LoginController endpoint; real: " + real);
    }

    @Test
    void registerIsNotPubliclyReserved() {
        assertFalse(AdminSaTokenConfig.PUBLIC_PATHS.contains("/admin/auth/register"));
    }

    @Test
    void sensitiveAuthEndpointsStayBehindLogin() {
        // these resolve the caller from the session — they must never be on the public list
        for (String path : List.of("/admin/auth/logout", "/admin/auth/getLoginUserInfo", "/admin/auth/getRouters",
                "/admin/auth/get-async-routes")) {
            assertFalse(AdminSaTokenConfig.PUBLIC_PATHS.contains(path), path);
        }
    }

    private static Set<String> loginControllerPaths() {
        String base = Objects.requireNonNull(
                AnnotatedElementUtils.findMergedAnnotation(LoginController.class, RequestMapping.class)).path()[0];
        Set<String> paths = new TreeSet<>();
        for (Method method : LoginController.class.getDeclaredMethods()) {
            RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
            if (mapping != null) {
                for (String path : mapping.path()) {
                    paths.add(base + path);
                }
            }
        }
        return paths;
    }
}
