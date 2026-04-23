package com.lesofn.archsmith.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jspecify.annotations.Nullable;

/**
 * 密码策略校验器实现。
 *
 * @author sofn
 */
public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private int minLength;
    private int maxLength;
    private boolean requireDigit;
    private boolean requireLetter;
    private boolean requireSpecial;

    @Override
    public void initialize(ValidPassword constraint) {
        this.minLength = constraint.minLength();
        this.maxLength = constraint.maxLength();
        this.requireDigit = constraint.requireDigit();
        this.requireLetter = constraint.requireLetter();
        this.requireSpecial = constraint.requireSpecial();
    }

    @Override
    public boolean isValid(@Nullable String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        int length = value.length();
        if (length < minLength || length > maxLength) {
            return false;
        }

        boolean hasDigit = false;
        boolean hasLetter = false;
        boolean hasSpecial = false;

        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (Character.isLetter(c)) {
                hasLetter = true;
            } else {
                hasSpecial = true;
            }
        }

        if (requireDigit && !hasDigit) {
            return false;
        }
        if (requireLetter && !hasLetter) {
            return false;
        }
        if (requireSpecial && !hasSpecial) {
            return false;
        }
        return true;
    }
}
