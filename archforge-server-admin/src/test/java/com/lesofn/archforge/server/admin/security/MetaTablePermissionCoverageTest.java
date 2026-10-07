package com.lesofn.archforge.server.admin.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import com.lesofn.archforge.server.admin.controller.metatable.MetaTableController;
import com.lesofn.archforge.server.admin.controller.metatable.MetaTableDesignerController;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Meta-table endpoints read, rewrite and export arbitrary business tables: every handler must
 * carry its own method-level permission. {@code AdminPermissionCoverageTest} only proves a
 * class-level login/role guard — which let {@code export} ship open to every ADMIN.
 */
@Tag("contract")
class MetaTablePermissionCoverageTest {

    @Test
    void everyMetaTableHandlerDeclaresAMethodLevelPermission() {
        List<String> unguarded = new ArrayList<>();
        for (Class<?> controller : List.of(MetaTableController.class, MetaTableDesignerController.class)) {
            for (Method method : controller.getDeclaredMethods()) {
                if (!AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class)) {
                    continue;
                }
                SaCheckPermission permission = method.getAnnotation(SaCheckPermission.class);
                if (permission == null || !StpAdminUtil.TYPE.equals(permission.type()) || Arrays.stream(permission.value())
                        .noneMatch(v -> v.startsWith("meta-table:"))) {
                    unguarded.add(controller.getSimpleName() + "#" + method.getName());
                }
            }
        }

        assertEquals(List.of(), unguarded, "handlers without a meta-table:* @SaCheckPermission");
    }

    @Test
    void exportRequiresTheExportPermission() {
        Method export = Arrays.stream(MetaTableController.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("export"))
                .findFirst()
                .orElseThrow();

        SaCheckPermission permission = export.getAnnotation(SaCheckPermission.class);

        assertNotNull(permission, "export must not be reachable by every ADMIN");
        assertEquals(List.of("meta-table:export"), Arrays.asList(permission.value()));
        assertEquals(StpAdminUtil.TYPE, permission.type());
    }
}
