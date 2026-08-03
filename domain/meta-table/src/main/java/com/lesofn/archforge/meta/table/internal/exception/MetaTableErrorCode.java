package com.lesofn.archforge.meta.table.internal.exception;

import com.lesofn.archforge.common.error.ArchForgeProjectModule;
import com.lesofn.archforge.common.error.api.ErrorCode;
import com.lesofn.archforge.common.error.manager.ErrorManager;
import lombok.Getter;

/**
 * 元表格错误码。
 */
@Getter
public enum MetaTableErrorCode implements ErrorCode {
    META_TABLE_NOT_EXISTS(1, "元表格不存在"),
    META_TABLE_CODE_EXISTS(2, "表格编码已存在"),
    META_TABLE_CODE_INVALID(3, "表格编码非法"),
    META_COLUMN_CODE_INVALID(4, "字段编码非法"),
    META_TABLE_HAS_DATA(5, "元表格中仍存在{0}条数据"),
    META_TABLE_DATA_NOT_EXISTS(6, "数据不存在"),
    META_COLUMN_TYPE_INVALID(7, "字段类型非法"),
    META_COLUMN_VALUE_INVALID(8, "字段值校验失败：{0}");

    private final int nodeNum;
    private final String msg;

    MetaTableErrorCode(int nodeNum, String msg) {
        this.nodeNum = nodeNum;
        this.msg = msg;
        ErrorManager.register(ArchForgeProjectModule.META_TABLE, this);
    }
}
