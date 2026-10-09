package com.lesofn.archforge.meta.table.api.service;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 导出元表格迁移记录为 Flyway 兼容的 SQL 文件。
 */
public interface MetaTableMigrationExporter {

    /**
     * 把某张元表格的全部迁移记录导出为 Flyway SQL 文件。
     *
     * @param tableId 元表格 ID
     * @param outputDir 输出目录（例如 db/migration）
     * @return 生成的文件路径
     */
    Path export(Long tableId, Path outputDir) throws IOException;
}
