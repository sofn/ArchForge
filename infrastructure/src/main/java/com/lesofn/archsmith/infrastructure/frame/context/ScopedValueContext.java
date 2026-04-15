package com.lesofn.archsmith.infrastructure.frame.context;

import jakarta.servlet.http.HttpServletRequest;

/**
 * JDK 25 ScopedValue-based request context propagation.
 *
 * <p>Replaces ThreadLocal-based context: automatic cleanup, virtual-thread friendly, no memory
 * leaks.
 *
 * @author sofn
 */
public final class ScopedValueContext {

    private static final ScopedValue<RequestContext> CONTEXT = ScopedValue.newInstance();

    private ScopedValueContext() {}

    /**
     * Run an action within a request context scope. The context is automatically unbound when the
     * scope exits.
     */
    public static void runInScope(RequestContext ctx, ThrowingRunnable action) throws Exception {
        ScopedValue.where(CONTEXT, ctx)
                .call(
                        () -> {
                            action.run();
                            return null;
                        });
    }

    /** Get the current request context. Returns null if not in a scoped context. */
    public static RequestContext getRequestContext() {
        return CONTEXT.isBound() ? CONTEXT.get() : null;
    }

    /** Get the current servlet request, or null if not in scope. */
    public static HttpServletRequest getServletRequest() {
        RequestContext ctx = getRequestContext();
        return ctx != null ? ctx.getOriginRequest() : null;
    }

    /** Check if a request context is currently bound. */
    public static boolean isBound() {
        return CONTEXT.isBound();
    }

    /** Functional interface that allows checked exceptions. */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}
