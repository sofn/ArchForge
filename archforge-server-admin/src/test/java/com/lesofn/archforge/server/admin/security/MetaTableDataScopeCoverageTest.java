package com.lesofn.archforge.server.admin.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lesofn.archforge.infrastructure.security.datascope.DataPermission;
import com.lesofn.archforge.server.admin.controller.metatable.MetaTableController;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Row-level scope only applies inside a {@code @DataPermission} call — without one the meta-table filter sees no
 * scope and returns every row. Every row-data handler must therefore carry the annotation; forgetting it on a new
 * endpoint would silently drop row-level permissions while the build stays green.
 */
@Tag("contract")
class MetaTableDataScopeCoverageTest {

    @Test
    void everyRowDataHandlerRunsUnderTheCallersDataScope() {
        List<String> unscoped = new ArrayList<>();
        for (Method method : MetaTableController.class.getDeclaredMethods()) {
            if (AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class) && !AnnotatedElementUtils.hasAnnotation(
                    method, DataPermission.class)) {
                unscoped.add(method.getName());
            }
        }

        assertEquals(List.of(), unscoped, "MetaTableController handlers without @DataPermission");
    }
}
