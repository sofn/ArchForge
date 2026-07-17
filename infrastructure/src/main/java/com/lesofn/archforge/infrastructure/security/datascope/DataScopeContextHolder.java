package com.lesofn.archforge.infrastructure.security.datascope;

import com.lesofn.archforge.infrastructure.frame.context.RequestContext;
import com.lesofn.archforge.infrastructure.frame.context.ScopedValueContext;

/**
 * 数据权限上下文持有者，基于 {@link ScopedValueContext} 的请求上下文属性传递。
 *
 * @author sofn
 */
public final class DataScopeContextHolder {

    private static final String KEY = DataScopeContext.class.getName();

    private DataScopeContextHolder() {
    }

    /**
     * 获取当前请求线程的数据权限上下文。
     *
     * @return 上下文，未绑定或不存在时返回 null
     */
    public static DataScopeContext get() {
        RequestContext requestContext = ScopedValueContext.getRequestContext();
        if (requestContext == null) {
            return null;
        }
        Object value = requestContext.getAttribute(KEY);
        return value instanceof DataScopeContext ? (DataScopeContext) value : null;
    }

    /**
     * 设置数据权限上下文到当前请求。
     */
    public static void set(DataScopeContext context) {
        RequestContext requestContext = ScopedValueContext.getRequestContext();
        if (requestContext != null) {
            requestContext.setAttribute(KEY, context);
        }
    }

    /**
     * 清理当前请求的数据权限上下文。
     */
    public static void clear() {
        RequestContext requestContext = ScopedValueContext.getRequestContext();
        if (requestContext != null) {
            requestContext.setAttribute(KEY, null);
        }
    }
}
