package com.lesofn.archforge.meta.table.internal.service;

import com.lesofn.archforge.meta.table.api.dao.MetaTableMigrationRepository;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.domain.MetaTableMigration;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 导出元表格迁移记录为 Flyway 兼容的 SQL 文件。
 */
@Service
@RequiredArgsConstructor
public class MetaTableMigrationExporter {

    private static final Pattern FLYWAY_VERSION_PATTERN = Pattern.compile("^V(\\d+)__.*\\.sql$");

    private final MetaTableAdminService metaTableAdminService;
    private final MetaTableMigrationRepository migrationRepository;

    /**
     * 把某张元表格的全部迁移记录导出为 Flyway SQL 文件。
     *
     * @param tableId   元表格 ID
     * @param outputDir 输出目录（例如 db/migration）
     * @return 生成的文件路径
     */
    public Path export(Long tableId, Path outputDir) throws IOException {
        MetaTable table = metaTableAdminService.findById(tableId);
        List<MetaTableMigration> migrations = migrationRepository
                .findByTableIdAndDeletedFalseOrderByVersionAsc(tableId);

        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        int nextVersion = resolveNextFlywayVersion(outputDir);
        String fileName = String.format("V%d__meta_%s_schema.sql", nextVersion, table.getTableCode());
        Path target = outputDir.resolve(fileName);

        StringBuilder sb = new StringBuilder();
        sb.append("-- Flyway migration for ").append(table.getTableCode()).append("\n");
        for (MetaTableMigration m : migrations) {
            sb.append("-- version ").append(m.getVersion())
                    .append(", ").append(m.getChangeType())
                    .append(" [").append(m.getColumnCode()).append("]\n");
            sb.append(m.getDdlSql()).append(";\n");
        }

        Files.writeString(target, sb.toString(), StandardCharsets.UTF_8);
        return target;
    }

    private int resolveNextFlywayVersion(Path outputDir) throws IOException {
        int max = 0;
        if (!Files.exists(outputDir)) {
            return max + 1;
        }
        try (Stream<Path> files = Files.list(outputDir)) {
            for (Path file : files.toList()) {
                String name = file.getFileName().toString();
                Matcher matcher = FLYWAY_VERSION_PATTERN.matcher(name);
                if (matcher.matches()) {
                    int version = Integer.parseInt(matcher.group(1));
                    if (version > max) {
                        max = version;
                    }
                }
            }
        }
        return max + 1;
    }
}
