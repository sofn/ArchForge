package com.lesofn.archforge.infrastructure.frame.utils.log;

import org.jspecify.annotations.Nullable;
import com.lesofn.archforge.common.profile.DefaultProfileLoader;
import com.lesofn.archforge.common.utils.jackson.JsonUtil;
import tools.jackson.databind.node.ObjectNode;
import com.lesofn.archforge.infrastructure.frame.context.RequestContext;
import com.lesofn.archforge.infrastructure.frame.context.ScopedValueContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Authors: sofn Version: 1.0 Created at 15-6-7 21:00. */
public class ApiLogger {
    private static Logger log = LoggerFactory.getLogger("debug");
    private static Logger infoLog = LoggerFactory.getLogger("info");
    private static Logger warnLog = LoggerFactory.getLogger("warn");
    private static Logger errorLog = LoggerFactory.getLogger("error");

    private static Logger fireLog = LoggerFactory.getLogger("fire");

    private static @Nullable String getRequestId() {
        RequestContext ctx = ScopedValueContext.getRequestContext();
        return ctx == null ? "-" : ctx.getRequestId();
    }

    public static boolean isTraceEnabled() { return log.isTraceEnabled() && !DefaultProfileLoader.isProd(); }

    public static boolean isDebugEnabled() { return log.isDebugEnabled() && !DefaultProfileLoader.isProd(); }

    public static void trace(Object msg) {
        log.trace(formatMsg(msg));
    }

    public static void trace(String tag, Object msg) {
        log.trace(formatMsg(tag, msg));
    }

    public static void debug(Object msg) {
        if (log.isDebugEnabled() && !DefaultProfileLoader.isProd()) {
            log.debug(formatMsg(msg));
        }
    }

    private static String formatMsg(@Nullable Object msg) {
        return String.format(
                "%s\t%s",
                getRequestId(),
                msg == null ? "null" : msg.toString());
    }

    private static String formatMsg(String tag, @Nullable Object msg) {
        return String.format(
                "%s\t%s\t%s",
                getRequestId(),
                tag,
                msg == null ? "null" : msg.toString());
    }

    public static void debug(String tag, Object msg) {
        if (msg == null) {
            return;
        }
        // 非生产环境打印debug日志
        if (log.isDebugEnabled() && !DefaultProfileLoader.isProd()) {
            log.debug(formatMsg(tag, msg));
        }
    }

    /**
     * 记录慢请求日志。
     *
     * @param resourceType 资源类型
     * @param resourceId 资源id 如 服务器ip端口，接口的 url地址等
     * @param useTime 耗费时间
     */
    public static void fireSlow(String resourceType, String resourceId, long useTime) {
        fireSlow(resourceType, resourceId, useTime, null);
    }

    public static void fireSlow(
            String resourceType, String resourceId, long useTime, @Nullable ObjectNode ext) {
        String extStr = "";
        if (ext != null) {
            try {
                extStr = JsonUtil.getObjectMapper().writeValueAsString(ext);
            } catch (Exception e) {
                // Ignore serialization errors
            }
        }
        fireLog.warn(
                String.format(
                        "%s\t%s\t%s\t%s\t%s\t%s",
                        getRequestId(),
                        resourceType,
                        resourceId,
                        "slow",
                        useTime,
                        extStr));
    }

    /**
     * 记录错误日志。
     *
     * @param resourceType 资源类型
     * @param resourceId 资源id
     * @param msg 错误消息
     * @param e exception
     */
    public static void fireError(String resourceType, String resourceId, String msg, Exception e) {
        fireLog.error(
                String.format(
                        "%s\t%s\t%s\t%s\t%s\t%s",
                        getRequestId(),
                        resourceType,
                        resourceId,
                        "error",
                        msg,
                        e.getClass().getName()));
    }

    public static void info(Object msg) {
        if (msg == null) {
            return;
        }
        if (infoLog.isInfoEnabled()) {
            infoLog.info(formatMsg(msg));
        }
    }

    public static void info(String tag, Object msg) {
        if (msg == null) {
            return;
        }
        if (infoLog.isInfoEnabled()) {
            infoLog.info(formatMsg(tag, msg));
        }
    }

    public static void warn(Object msg) {
        warnLog.warn(formatMsg(msg));
    }

    public static void error(Object msg) {
        if (msg instanceof Throwable throwable) {
            errorLog.error(formatMsg(throwable.getMessage()), throwable);
        } else {
            errorLog.error(formatMsg(msg));
        }
    }

    public static void error(Object msg, Throwable e) {
        errorLog.error(formatMsg(msg), e);
    }
}
