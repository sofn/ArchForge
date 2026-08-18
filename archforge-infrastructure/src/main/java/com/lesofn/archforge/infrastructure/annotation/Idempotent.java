package com.lesofn.archforge.infrastructure.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 幂等注解。
 *
 * <p>
 * 支持三种模式：
 * <ul>
 * <li>{@code PARAM}：基于方法参数生成 Redis key，SETNX + TTL 防止重复提交</li>
 * <li>{@code TOKEN}：客户端先申请一次性幂等 Token，请求时通过 {@code X-Idempotent-Token}
 * 头部传入，服务端消费后失效</li>
 * <li>{@code HEADER}：基于指定请求头值做幂等控制</li>
 * </ul>
 *
 * @author sofn
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /** 幂等模式 */
    IdempotentType type() default IdempotentType.PARAM;

    /**
     * 幂等 key。
     *
     * <p>
     * {@code PARAM} 模式下支持 SpEL 表达式（如 {@code #orderNo}），为空时使用方法签名 + 参数哈希。
     * {@code HEADER} 模式下为请求头名称前缀，可为空。
     */
    String key() default "";

    /** 幂等 Token / key 过期时间（秒，默认 10 秒；TOKEN 模式下指 Token 申请后的有效期） */
    long expireSeconds() default 10;

    /** 提示信息 */
    String message() default "";

    /** TOKEN 模式下读取的 HTTP Header 名称 */
    String header() default "X-Idempotent-Token";
}
