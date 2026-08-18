package com.lesofn.archforge.infrastructure.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 防止重复提交注解。
 *
 * <p>
 * 参考 RuoYi-Vue，基于 URL + 请求参数 + 时间间隔判断是否为重复提交。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {

    /** 间隔时间（毫秒），小于此时间视为重复提交 */
    int interval() default 5000;

    /** 国际化消息 key（也支持兜底直接文案） */
    String message() default "repeat.submit.default";
}
