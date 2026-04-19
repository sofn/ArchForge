package com.lesofn.archsmith.infrastructure.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 防重复提交注解。
 *
 * <p>AOP 切面基于 Redis SETNX + TTL 实现，key 由用户标识 + 方法签名 + 参数哈希组成。
 *
 * @author sofn
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /** 重复提交检查时长（秒，默认 5 秒） */
    int interval() default 5;

    /** Key 前缀（默认使用方法签名） */
    String keyPrefix() default "";
}
