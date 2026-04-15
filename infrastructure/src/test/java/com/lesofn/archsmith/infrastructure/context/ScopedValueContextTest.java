package com.lesofn.archsmith.infrastructure.context;

import static org.junit.jupiter.api.Assertions.*;

import com.lesofn.archsmith.infrastructure.frame.context.RequestContext;
import com.lesofn.archsmith.infrastructure.frame.context.ScopedValueContext;
import java.util.concurrent.StructuredTaskScope;
import org.junit.jupiter.api.Test;

/** ScopedValue context propagation tests (replaces ThreadLocal tests). */
public class ScopedValueContextTest {

    @Test
    public void testScopedValueAutoCleanup() throws Exception {
        RequestContext ctx = new RequestContext("test-request-1");
        ctx.setCurrentUid(42);

        ScopedValueContext.runInScope(
                ctx,
                () -> {
                    RequestContext bound = ScopedValueContext.getRequestContext();
                    assertNotNull(bound);
                    assertEquals("test-request-1", bound.getRequestId());
                    assertEquals(42, bound.getCurrentUid());
                });

        // After scope exits, context is automatically unbound
        assertNull(ScopedValueContext.getRequestContext());
        assertFalse(ScopedValueContext.isBound());
    }

    @Test
    public void testScopedValueVirtualThreadInheritance() throws Exception {
        RequestContext ctx = new RequestContext("parent-request");
        ctx.setCurrentUid(100);

        ScopedValueContext.runInScope(
                ctx,
                () -> {
                    // ScopedValue automatically propagates to StructuredTaskScope child threads
                    try (var scope = StructuredTaskScope.open()) {
                        var task1 =
                                scope.fork(
                                        () -> {
                                            RequestContext childCtx =
                                                    ScopedValueContext.getRequestContext();
                                            assertNotNull(childCtx);
                                            assertEquals("parent-request", childCtx.getRequestId());
                                            return childCtx.getRequestId();
                                        });
                        var task2 =
                                scope.fork(
                                        () -> {
                                            RequestContext childCtx =
                                                    ScopedValueContext.getRequestContext();
                                            assertNotNull(childCtx);
                                            assertEquals(100, childCtx.getCurrentUid());
                                            return childCtx.getCurrentUid();
                                        });
                        scope.join();
                        assertEquals("parent-request", task1.get());
                        assertEquals(100L, task2.get());
                    }
                });
    }

    @Test
    public void testUnboundReturnsNull() {
        // Outside any scope, getRequestContext returns null
        assertNull(ScopedValueContext.getRequestContext());
        assertFalse(ScopedValueContext.isBound());
    }
}
