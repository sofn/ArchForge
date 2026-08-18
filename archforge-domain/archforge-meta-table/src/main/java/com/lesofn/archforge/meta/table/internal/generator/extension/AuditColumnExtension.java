package com.lesofn.archforge.meta.table.internal.generator.extension;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.internal.generator.CodeGenOptions;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "arch-forge.codegen.audit-enabled", havingValue = "true")
public class AuditColumnExtension implements CodeGenExtension {

    @Override
    public String getName() { return "audit-column"; }

    @Override
    public void beforeBuildModel(MetaTable table, List<MetaColumn> columns, CodeGenOptions options, Map<String, Object> model) {
        MetaColumn auditStatus = new MetaColumn();
        auditStatus.setColumnCode("audit_status");
        auditStatus.setColumnName("审计状态");
        auditStatus.setDataType(MetaColumnType.INTEGER);
        auditStatus.setSearchable(false);
        auditStatus.setListVisible(true);
        auditStatus.setRequired(false);
        auditStatus.setNullable(true);
        columns.add(auditStatus);

        MetaColumn auditRemark = new MetaColumn();
        auditRemark.setColumnCode("audit_remark");
        auditRemark.setColumnName("审计备注");
        auditRemark.setDataType(MetaColumnType.TEXT);
        auditRemark.setSearchable(true);
        auditRemark.setListVisible(true);
        auditRemark.setRequired(false);
        auditRemark.setNullable(true);
        columns.add(auditRemark);
    }

    @Override
    public void postProcess(Path backendDir, Path frontendDir, Map<String, Object> model, List<Path> generatedFiles) {
        try {
            Path marker = backendDir.resolve("AUDIT.md");
            Files.createDirectories(marker.getParent());
            Files.writeString(marker, "# Generated with AuditColumnExtension\n", StandardCharsets.UTF_8);
            generatedFiles.add(marker);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write audit marker", e);
        }
    }
}
