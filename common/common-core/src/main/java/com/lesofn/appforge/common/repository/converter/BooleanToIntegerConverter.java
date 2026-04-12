package com.lesofn.appforge.common.repository.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 数据库 1/0 与 Java Boolean 的转换器
 * 数据库: 1 表示 true, 0 表示 false
 * Java: Boolean true/false
 */
@Converter(autoApply = true)
public class BooleanToIntegerConverter implements AttributeConverter<Boolean, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Boolean attribute) {
        if (attribute == null) {
            return 0;
        }
        return attribute ? 1 : 0;
    }

    @Override
    public Boolean convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return false;
        }
        return dbData == 1;
    }
}
