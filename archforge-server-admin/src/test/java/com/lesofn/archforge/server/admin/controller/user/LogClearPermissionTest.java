package com.lesofn.archforge.server.admin.controller.user;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import cn.dev33.satoken.annotation.SaCheckPermission;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;

/** Wiping an audit log is a removal: a read-only role (":list") must not be able to do it. */
class LogClearPermissionTest {

    @Test
    void clearingOperationLogsNeedsTheRemovePermission() {
        assertArrayEquals(new String[] {
                "monitor:operlog:remove"
        }, clearPermission(OperationLogController.class));
    }

    @Test
    void clearingLoginLogsNeedsTheRemovePermission() {
        assertArrayEquals(new String[] {
                "monitor:logininfor:remove"
        }, clearPermission(LoginLogController.class));
    }

    private static String[] clearPermission(Class<?> controller) {
        Method clear = Arrays.stream(controller.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(PostMapping.class))
                .filter(m -> Arrays.asList(m.getAnnotation(PostMapping.class).value()).contains("/clear"))
                .findFirst()
                .orElseThrow();
        return Objects.requireNonNull(clear.getAnnotation(SaCheckPermission.class)).value();
    }
}
