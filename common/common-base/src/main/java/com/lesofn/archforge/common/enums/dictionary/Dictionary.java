package com.lesofn.archforge.common.enums.dictionary;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Dictionary {

    /** 字典类型编码 */
    String name();

    /** 字典类型显示名称 */
    String label() default "";

    /** 描述 */
    String description() default "";
}
