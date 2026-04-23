package com.lesofn.archsmith.infrastructure.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 限流注解。
 *
 * <p>AOP 切面基于 Redis + Lua 脚本原子自增实现，支持按全局/IP/用户维度限流。
 *
 * @author sofn
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 限流 key，默认空字符串时使用方法签名 */
    String key() default "";

    /** 时间窗口（秒） */
    int time() default 60;

    /** 最大请求次数 */
    int maxCount() default 100;

    /** 限流维度 */
    LimitType limitType() default LimitType.GLOBAL;

    enum LimitType {
        /** 全局限流 */
        GLOBAL,
        /** 按 IP 限流 */
        IP,
        /** 按登录用户限流 */
        USER
    }
}
