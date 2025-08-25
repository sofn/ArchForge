package com.lesofn.matrixboot.common.enums;

import com.lesofn.matrixboot.common.error.system.SystemErrorCode;
import com.lesofn.matrixboot.common.error.system.SystemException;
import org.apache.commons.lang3.BooleanUtils;

import java.util.Objects;

/**
 * @author valarchie
 */
public class BasicEnumUtil {

    private BasicEnumUtil() {
    }

    public static final String UNKNOWN = "未知";

    public static <E extends Enum<E>> E fromValueSafely(Class<E> enumClass, Object value) {
        E target = null;

        for (E enumConstant : enumClass.getEnumConstants()) {
            BasicEnum<?> basicEnum = (BasicEnum<?>) enumConstant;
            if (Objects.equals(basicEnum.getValue(), value)) {
                target = (E) basicEnum;
            }
        }

        return target;
    }

    public static <E extends Enum<E>> E fromValue(Class<E> enumClass, Object value) {
        E target = null;

        for (E enumConstant : enumClass.getEnumConstants()) {
            BasicEnum basicEnum = (BasicEnum) enumConstant;
            if (Objects.equals(basicEnum.getValue(), value)) {
                target = (E) basicEnum;
            }
        }

        if (target == null) {
            throw new SystemException(SystemErrorCode.GET_ENUM_FAILED, enumClass.getName());
        }

        return target;
    }

    public static <E extends Enum<E>> String getDescriptionByBool(Class<E> enumClass, Boolean bool) {
        Integer value = BooleanUtils.toInteger(bool);
        return getDescriptionByValue(enumClass, value);
    }

    public static <E extends Enum<E>> String getDescriptionByValue(Class<E> enumClass, Object value) {
        E basicEnum = fromValueSafely(enumClass, value);
        if (basicEnum != null) {
            return ((BasicEnum<?>) basicEnum).description();
        }
        return UNKNOWN;
    }

}
