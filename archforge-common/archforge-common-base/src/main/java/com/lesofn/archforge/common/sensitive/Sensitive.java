package com.lesofn.archforge.common.sensitive;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据脱敏注解，标注在需要脱敏的字段上
 *
 * <p>
 * 示例：
 *
 * <pre>
 * &#64;Sensitive(SensitiveType.PHONE)
 * private String phoneNumber;
 * </pre>
 *
 * @author sofn
 */
@Target({
        ElementType.FIELD, ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sensitive {

    /** 脱敏类型 */
    SensitiveType value();
}
