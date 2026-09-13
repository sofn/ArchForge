package com.lesofn.archforge.meta.table.api.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.jspecify.annotations.Nullable;

/**
 * 物理表导入预览：列级类型映射结果与整体兼容性。
 */
@Data
@SuppressWarnings("NullAway.Init")
public class TableImportPreview {

    private String tableName;

    private @Nullable String comment;

    private boolean compatible;

    private List<String> reasons = new ArrayList<>();

    private List<PreviewColumn> columns = new ArrayList<>();

    /** 复合索引名列表（v1 不导入，仅展示）。 */
    private List<String> compositeIndexes = new ArrayList<>();

    /**
     * 列级映射预览。
     */
    @Data
    @SuppressWarnings("NullAway.Init")
    public static class PreviewColumn {

        /** 物理列名（导入后即 columnCode）。 */
        private String columnCode;

        /** 列注释，导入后为 columnName 默认值。 */
        private @Nullable String comment;

        /** 是否为固定审计列（id/creator_id/...，不生成 MetaColumn）。 */
        private boolean audit;

        /** 映射出的 MetaColumnType 名；audit 列或不可映射时为 null。 */
        private @Nullable String dataType;

        private @Nullable Integer length;

        private @Nullable Integer precision;

        private @Nullable Integer scale;

        /** NOT NULL 且无默认值 → required=true。 */
        private boolean required;

        private boolean unique;

        private boolean indexed;

        /** 列序（ordinal_position），导入后为 sort。 */
        private int sort;

        /** 不可映射原因或提示信息。 */
        private @Nullable String warning;
    }
}
