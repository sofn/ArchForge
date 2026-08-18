package com.lesofn.archforge.infrastructure.annotation;

/**
 * 幂等模式。
 *
 * @author sofn
 */
public enum IdempotentType {

    /** 基于方法参数生成 Redis key */
    PARAM,

    /** 基于一次性 Token */
    TOKEN,

    /** 基于请求头值 */
    HEADER
}
