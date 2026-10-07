package com.lesofn.archforge.infrastructure.security.datascope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.common.auth.DataScopeEnum;
import com.lesofn.archforge.infrastructure.frame.context.RequestContext;
import com.lesofn.archforge.infrastructure.frame.context.ScopedValueContext;
import java.util.Objects;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;

/**
 * A {@code @DataPermission} call whose user cannot be resolved used to run with no data scope at all — every row
 * visible. It must fail closed: ONLY_SELF without a user id, which both the JPA specification and the meta-table filter
 * turn into "no rows".
 */
class DataScopeAspectTest {

    private final DataScopeAspect aspect = new DataScopeAspect();

    @DataPermission(deptAlias = "deptId", userAlias = "id")
    @SuppressWarnings("unused")
    private void annotated() {
    }

    @Test
    void unresolvableUserFailsClosed() throws Throwable {
        DataPermission permission = Objects.requireNonNull(
                DataScopeAspectTest.class.getDeclaredMethod("annotated").getAnnotation(DataPermission.class));
        ProceedingJoinPoint call = mock(ProceedingJoinPoint.class);
        when(call.proceed()).thenAnswer(invocation -> DataScopeContextHolder.get());

        DataScopeContext[] seen = new DataScopeContext[1];
        // a request scope, but no sa-token session: LoginContext.getAdminUser() cannot resolve anyone
        ScopedValueContext.runInScope(new RequestContext("req-1"), () -> {
            try {
                seen[0] = (DataScopeContext) aspect.around(call, permission);
            } catch (Throwable t) {
                throw new IllegalStateException(t);
            }
            assertNull(DataScopeContextHolder.get(), "cleared after the call");
        });

        assertNotNull(seen[0], "a scope must be set even without a user");
        assertEquals(DataScopeEnum.ONLY_SELF, seen[0].getDataScope());
        assertNull(seen[0].getUserId());
    }
}
