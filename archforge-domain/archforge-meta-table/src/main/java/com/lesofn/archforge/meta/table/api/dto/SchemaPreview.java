package com.lesofn.archforge.meta.table.api.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.jspecify.annotations.Nullable;

/**
 * Schema 变更预览：diff 明细 + 每条变更的违规行数与处置方式。
 * 由 {@code POST /meta-table/{id}/schema-preview} 返回 —— 只读，不执行任何 DDL。
 */
@Data
@SuppressWarnings("NullAway.Init")
public class SchemaPreview {

    /** 有序变更列表（与提交时执行顺序一致）。 */
    private List<PreviewChange> changes = new ArrayList<>();

    /** 任一变更危险（丢数据/阻断/需回填）即为 true。 */
    private boolean dangerous;

    @Data
    @SuppressWarnings("NullAway.Init")
    public static class PreviewChange {

        /**
         * SchemaChangeType 名：ADD_COLUMN / DROP_COLUMN / RENAME_COLUMN / ALTER_TYPE / ALTER_DEFAULT / ALTER_NULL / ALTER_INDEX。
         */
        private String type;

        /** 受影响字段编码（新列取新，DROP/RENAME 取旧）。 */
        private @Nullable String columnCode;

        /** 仅 RENAME_COLUMN：变更前编码。 */
        private @Nullable String oldColumnCode;

        private @Nullable String oldType;

        private @Nullable String newType;

        private @Nullable String oldDefault;

        private @Nullable String newDefault;

        /** 仅 ALTER_NULL：变更前可空性。 */
        private @Nullable Boolean oldNullable;

        /** 仅 ALTER_NULL：变更后可空性。 */
        private @Nullable Boolean newNullable;

        /** 违反新约束的存量行数（只读 COUNT）。 */
        private long violations;

        /** NONE = 安全；BACKFILL = 将用默认值回填违规行；BLOCKED = 提交时会被拒绝。 */
        private String action;

        /** 该变更将执行的 DDL 语句（预览展示用）。 */
        private List<String> ddl = new ArrayList<>();
    }
}
