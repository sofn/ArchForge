package com.lesofn.archforge.meta.table.api.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.jspecify.annotations.Nullable;

/**
 * 可导入的物理表信息（兼容性评估结果）。
 */
@Data
@SuppressWarnings("NullAway.Init")
public class ImportableTableInfo {

    /** 物理表名。 */
    private String tableName;

    /** 表注释。 */
    private @Nullable String comment;

    /** 列数（含审计列）。 */
    private int columnCount;

    /** 估算行数（pg_class.reltuples，-1 表示无统计）。 */
    private long estimatedRows;

    /** 是否满足纳管兼容条件。 */
    private boolean compatible;

    /** 不兼容原因列表（compatible=true 时为空）。 */
    private List<String> reasons = new ArrayList<>();

    /** 已注册为 meta-table。 */
    private boolean registered;
}
