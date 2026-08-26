package com.lesofn.archforge.meta.table.api.domain;

import com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;

/**
 * 元表格字段数据类型。
 */
public enum MetaColumnType {
    STRING,
    TEXT,
    INTEGER,
    DECIMAL,
    BOOLEAN,
    DATE,
    DATETIME,
    ENUM,
    JSON,
    FILE,
    IMAGE,
    MULTI_IMAGE,
    UUID,
    TIMESTAMPTZ,
    ARRAY,
    GEO,
    REFERENCE;

    /** 解析字段类型字符串，非法值抛业务异常而非裸 IllegalArgumentException。 */
    public static MetaColumnType of(String value) {
        try {
            return MetaColumnType.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_TYPE_INVALID, "不支持的字段类型: " + value);
        }
    }
}
