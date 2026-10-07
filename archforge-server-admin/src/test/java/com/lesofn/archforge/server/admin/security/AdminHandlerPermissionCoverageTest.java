package com.lesofn.archforge.server.admin.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cn.dev33.satoken.annotation.SaCheckPermission;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Method-level permission coverage for every admin handler. The class-level ADMIN role is not an authorization model:
 * a handler without its own {@code @SaCheckPermission} is open to every console role (dictionary edits, the config
 * list with its values, role/permission enumeration and data exports all shipped that way).
 *
 * <p>
 * Handlers that are intentionally open to every logged-in console user are listed in {@link #EXEMPT} with the reason —
 * adding to that list is a reviewed decision, not a way to silence the test.
 */
@Tag("contract")
class AdminHandlerPermissionCoverageTest {

    private static final String CONTROLLER_PACKAGE = "com.lesofn.archforge.server.admin.controller";

    /** controller#method → why every logged-in console user may call it. */
    private static final Map<String, String> EXEMPT = Map.ofEntries(
            Map.entry("LoginController#getConfig", "login page bootstrap, login-free"),
            Map.entry("LoginController#getCaptchaImg", "login page, login-free"),
            Map.entry("LoginController#login", "login-free"),
            Map.entry("LoginController#logout", "ends the caller's own session"),
            Map.entry("LoginController#refreshToken", "login-free token refresh"),
            Map.entry("LoginController#getLoginUserInfo", "the caller's own profile and permissions"),
            Map.entry("LoginController#getRouters", "the caller's own menu routes"),
            Map.entry("LoginController#getAsyncRoutes", "the caller's own menu routes"));

    @Test
    void everyAdminHandlerDeclaresAPermissionOrAReviewedExemption() throws ClassNotFoundException {
        Set<String> unguarded = new TreeSet<>();
        for (Class<?> controller : controllers()) {
            boolean classGuarded = AnnotatedElementUtils.hasAnnotation(controller, SaCheckPermission.class);
            for (Method method : controller.getDeclaredMethods()) {
                if (!AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class)) {
                    continue;
                }
                String key = controller.getSimpleName() + "#" + method.getName();
                if (!classGuarded && !AnnotatedElementUtils.hasAnnotation(method, SaCheckPermission.class) && !EXEMPT
                        .containsKey(key)) {
                    unguarded.add(key);
                }
            }
        }

        assertEquals(List.of(), new ArrayList<>(unguarded), "admin handlers without @SaCheckPermission");
    }

    private static List<Class<?>> controllers() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        List<Class<?>> controllers = new ArrayList<>();
        for (BeanDefinition definition : scanner.findCandidateComponents(CONTROLLER_PACKAGE)) {
            controllers.add(Class.forName(Objects.requireNonNull(definition.getBeanClassName())));
        }
        return controllers;
    }
}
