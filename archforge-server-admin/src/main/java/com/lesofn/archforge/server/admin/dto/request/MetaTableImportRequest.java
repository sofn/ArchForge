package com.lesofn.archforge.server.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.jspecify.annotations.Nullable;

/**
 * 已有物理表导入请求
 */
@Data
@SuppressWarnings("NullAway.Init")
public class MetaTableImportRequest {

    /** 物理表名（须满足纳管兼容条件）。 */
    @NotBlank
    private String tableName;

    /** 显示名，空则用表注释或物理名。 */
    private @Nullable String displayName;

    private @Nullable String description;
}
