package com.lesofn.appengine.frame.utils.log;

import com.alibaba.fastjson.JSONObject;
import com.lesofn.appengine.common.profile.DefaultProfileLoader;
import com.lesofn.appengine.frame.context.ThreadLocalContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authors: sofn
 * Version: 1.0  Created at 15-6-7 21:00.
 */
public class ApiLogger {
    private static Logger log = LoggerFactory.getLogger("debug");
    private static Logger infoLog = LoggerFactory.getLogger("info");
    private static Logger warnLog = LoggerFactory.getLogger("warn");
    private static Logger errorLog = LoggerFactory.getLogger("error");

    private static Logger fireLog = LoggerFactory.getLogger("fire");

    public static boolean isTraceEnabled() {
        return log.isTraceEnabled() && !DefaultProfileLoader.isProd();
    }

    public static boolean isDebugEnabled() {
        return log.isDebugEnabled() && !DefaultProfileLoader.isProd();
    }

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

    private static String formatMsg(Object msg) {
        return String.format("%s\t%s", ThreadLocalContext.getRequestContext().getRequestId(), msg == null ? "null" : msg.toString());
    }

    private static String formatMsg(String tag, Object msg) {
        return String.format("%s\t%s\t%s", ThreadLocalContext.getRequestContext().getRequestId(), tag, msg == null ? "null" : msg.toString());
    }

    public static void debug(String tag, Object msg) {
        if (msg == null) {
            return;
        }
        //非生产环境打印debug日志
        if (log.isDebugEnabled() && !DefaultProfileLoader.isProd()) {
            log.debug(formatMsg(tag, msg));
        }
    }

    /**
     * @param resourceType 资源类型
     * @param resourceId   资源id 如 服务器ip端口，接口的 url地址等
     * @param useTime      耗费时间
     */
    public static void fireSlow(String resourceType, String resourceId, long useTime) {
        fireSlow(resourceType, resourceId, useTime, null);
    }

    public static void fireSlow(String resourceType, String resourceId, long useTime, JSONObject ext) {
        fireLog.warn(String.format("%s\t%s\t%s\t%s\t%s\t%s", ThreadLocalContext.getRequestContext().getRequestId(), resourceType, resourceId, "slow", useTime, ext == null ? "" : ext.toJSONString()));
    }

    /**
     * @param resourceType 资源类型
     * @param resourceId   资源id
     * @param msg          错误消息
     * @param e            exception
     */
    public static void fireError(String resourceType, String resourceId, String msg, Exception e) {
        fireLog.error(String.format("%s\t%s\t%s\t%s\t%s\t%s", ThreadLocalContext.getRequestContext().getRequestId(), resourceType, resourceId, "error", msg, e.getClass().getName()));
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
        if (msg instanceof Throwable) {
            errorLog.error(formatMsg(((Throwable) msg).getMessage()), (Throwable) msg);
        } else {
            errorLog.error(formatMsg(msg));
        }
    }

    public static void error(Object msg, Throwable e) {
        errorLog.error(formatMsg(msg), e);
    }

}
