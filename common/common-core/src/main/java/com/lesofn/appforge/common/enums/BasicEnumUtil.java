package com.lesofn.appforge.common.enums;

import com.lesofn.appforge.common.error.system.SystemException;
import com.lesofn.appforge.common.errors.SystemErrorCode;
import java.util.Objects;
import org.apache.commons.lang3.BooleanUtils;

/**
 * @author sofn
 */
public class BasicEnumUtil {

    private BasicEnumUtil() {}

    public static final String UNKNOWN = "未知";

    public static <E extends Enum<E>> E fromValueSafely(Class<E> enumClass, Object value) {
        E target = null;

        for (E enumConstant : enumClass.getEnumConstants()) {
            BasicEnum basicEnum = (BasicEnum) enumConstant;
            if (Objects.equals(basicEnum.getValue(), value)) {
                target = enumConstant;
            }
        }

        return target;
    }

    public static <E extends Enum<E>> E fromValue(Class<E> enumClass, Object value) {
        E target = null;

        for (E enumConstant : enumClass.getEnumConstants()) {
            BasicEnum basicEnum = (BasicEnum) enumConstant;
            if (Objects.equals(basicEnum.getValue(), value)) {
                target = enumConstant;
            }
        }

        if (target == null) {
            throw new SystemException(SystemErrorCode.GET_ENUM_FAILED, enumClass.getName());
        }

        return target;
    }

    public static <E extends Enum<E>> String getDescriptionByBool(
            Class<E> enumClass, Boolean bool) {
        Integer value = BooleanUtils.toInteger(bool);
        return getDescriptionByValue(enumClass, value);
    }

    public static <E extends Enum<E>> String getDescriptionByValue(
            Class<E> enumClass, Object value) {
        E basicEnum = fromValueSafely(enumClass, value);
        if (basicEnum != null) {
            return ((BasicEnum) basicEnum).getDescription();
        }
        return UNKNOWN;
    }
}
