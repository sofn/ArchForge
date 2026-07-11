package com.lesofn.archforge.infrastructure.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解。
 *
 * <p>
 * 标注在 Controller 方法上，方法执行完成后自动写入 sys_oper_log。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /** 模块名称。为空时尝试从所在类的 {@link io.swagger.v3.oas.annotations.tags.Tag} 读取。 */
    String module() default "";

    /** 操作摘要。为空时尝试从方法上的 {@link io.swagger.v3.oas.annotations.Operation} 读取。 */
    String summary() default "";
}
