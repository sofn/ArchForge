package com.lesofn.archsmith.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 密码安全策略校验注解。
 *
 * <p>默认要求：长度 8-32，必须包含至少 1 个数字、1 个字母、1 个特殊字符。
 *
 * @author sofn
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPasswordValidator.class)
public @interface ValidPassword {

    String message() default "密码不符合安全策略：长度 8-32，需包含数字、字母和特殊字符";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** 最小长度 */
    int minLength() default 8;

    /** 最大长度 */
    int maxLength() default 32;

    /** 是否必须包含数字 */
    boolean requireDigit() default true;

    /** 是否必须包含字母 */
    boolean requireLetter() default true;

    /** 是否必须包含特殊字符 */
    boolean requireSpecial() default true;
}
