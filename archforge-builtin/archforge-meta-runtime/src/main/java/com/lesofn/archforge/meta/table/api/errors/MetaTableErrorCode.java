package com.lesofn.archforge.meta.table.api.errors;

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
    // 占位符一律用 slf4j 风格的 {}（ErrorInfo 用 MessageFormatter 格式化），{0} 不会被替换
    META_TABLE_HAS_DATA(5, "元表格中仍存在{}条数据"),
    META_TABLE_DATA_NOT_EXISTS(6, "数据不存在"),
    META_COLUMN_TYPE_INVALID(7, "字段类型非法"),
    META_COLUMN_VALUE_INVALID(8, "字段值校验失败：{}"),
    META_TABLE_CONCURRENT_MODIFY(9, "表定义已被他人修改，请刷新后重试"),
    META_TABLE_EVOLUTION_INVALID(10, "Schema 演进预检失败：{}"),
    META_QUERY_PARAM_INVALID(11, "查询参数非法：{}"),
    META_DATA_SCOPE_DENIED(12, "数据超出数据范围：{}"),
    META_TABLE_IMPORT_INCOMPATIBLE(13, "物理表不满足纳管条件：{}"),
    META_TABLE_COLUMNS_REQUIRED(14, "字段列表不能为空；仅更新元信息请使用 PATCH /meta-table/{tableCode}"),
    META_DEFINITION_FILE_MANAGED(15, "元表格定义由定义文件管理（arch-forge.meta.source=file）：请修改定义 YAML 后重启"),
    META_TABLE_PREFIX_INVALID(16, "表前缀非法：{}"),
    META_PHYSICAL_TABLE_EXISTS(17, "物理表已存在：{}；已有物理表请使用「导入已有表」纳管");

    private final int nodeNum;
    private final String msg;

    MetaTableErrorCode(int nodeNum, String msg) {
        this.nodeNum = nodeNum;
        this.msg = msg;
        ErrorManager.register(ArchForgeProjectModule.META_TABLE, this);
    }
}
