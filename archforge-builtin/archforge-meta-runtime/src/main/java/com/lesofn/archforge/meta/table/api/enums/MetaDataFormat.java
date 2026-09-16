package com.lesofn.archforge.meta.table.api.enums;

import com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import java.util.Locale;

/**
 * 元表格数据导入导出文件格式。
 */
public enum MetaDataFormat {
    EXCEL,
    CSV,
    JSON;

    public static MetaDataFormat of(String value) {
        if (value == null || value.isEmpty()) {
            return EXCEL;
        }
        try {
            return MetaDataFormat.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "不支持的文件格式: " + value);
        }
    }
}
