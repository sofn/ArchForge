package com.lesofn.archforge.server.admin.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.lesofn.archforge.common.persistence.testsupport.AbstractIntegrationTest;
import com.lesofn.archforge.server.admin.Application;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.RestController;

/**
 * A permission that no menu or button grants can only ever be used by the super-admin wildcard: the role editor has
 * nothing to tick. That is how meta-table ({@code meta:table:*} vs {@code meta-table:*}, fixed by V21) and the
 * scheduler ({@code monitor:job:*}, never seeded) ended up unusable for every other role. Checked against the schema
 * Flyway actually builds.
 */
@SpringBootTest(classes = {
        Application.class
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("slow")
class PermissionSeedConsistencyIntegrationTest extends AbstractIntegrationTest {

    @Qualifier("metaTableJdbcTemplate")
    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Test
    void everyCheckedPermissionIsGrantableThroughAMenuOrButton() throws ClassNotFoundException {
        Set<String> seeded = new HashSet<>(jdbc.getJdbcOperations().queryForList(
                "SELECT permission FROM sys_menu WHERE deleted = 0 AND permission IS NOT NULL AND permission <> ''",
                String.class));

        Set<String> ungrantable = new TreeSet<>();
        for (Class<?> controller : controllers()) {
            for (Method method : controller.getDeclaredMethods()) {
                SaCheckPermission permission = AnnotatedElementUtils.findMergedAnnotation(method, SaCheckPermission.class);
                if (permission == null) {
                    continue;
                }
                for (String value : permission.value()) {
                    if (!seeded.contains(value)) {
                        ungrantable.add(value + " <- " + controller.getSimpleName() + "#" + method.getName());
                    }
                }
            }
        }

        assertEquals(List.of(), new ArrayList<>(ungrantable), "permissions no sys_menu row grants");
    }

    private static List<Class<?>> controllers() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        List<Class<?>> controllers = new ArrayList<>();
        for (BeanDefinition definition : scanner.findCandidateComponents("com.lesofn.archforge.server.admin")) {
            controllers.add(Class.forName(Objects.requireNonNull(definition.getBeanClassName())));
        }
        return controllers;
    }
}
