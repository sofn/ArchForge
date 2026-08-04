package com.lesofn.archforge.meta.table.internal.service;

import com.lesofn.archforge.meta.table.api.dao.MetaTableMigrationRepository;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.domain.MetaTableMigration;
import com.lesofn.archforge.meta.table.internal.ddl.SchemaDdl;
import com.lesofn.archforge.meta.table.internal.schema.SchemaChange;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 元表格 Schema 迁移记录服务。
 */
@Service
@RequiredArgsConstructor
public class MetaTableMigrationService {

    private final MetaTableMigrationRepository migrationRepository;

    /**
     * 为一次 Schema 演进创建 PENDING 迁移记录。
     *
     * @param table 元表格
     * @param version 本次迁移版本号
     * @param ddlList 生成的 DDL 语句集合（每条对应一个 SchemaChange）
     * @param operatorId 操作人 ID
     * @return 创建的记录列表
     */
    @Transactional("metaTableTransactionManager")
    public List<MetaTableMigration> createPendingRecords(
            MetaTable table, int version, List<SchemaDdl> ddlList, Long operatorId) {
        List<MetaTableMigration> records = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (SchemaDdl ddl : ddlList) {
            records.add(toMigration(table, version, ddl, operatorId, now));
        }
        return migrationRepository.saveAll(records);
    }

    public List<MetaTableMigration> listByTableId(Long tableId) {
        return migrationRepository.findByTableIdAndDeletedFalseOrderByVersionAsc(tableId);
    }

    @Transactional("metaTableTransactionManager")
    public List<MetaTableMigration> saveAll(List<MetaTableMigration> records) {
        return migrationRepository.saveAll(records);
    }

    private MetaTableMigration toMigration(MetaTable table, int version, SchemaDdl ddl, Long operatorId, LocalDateTime now) {
        MetaTableMigration record = new MetaTableMigration();
        record.setTableId(table.getId());
        record.setVersion(version);
        record.setChangeType(ddl.change().getType().name());
        record.setDdlSql(String.join(";\n", ddl.sqls()));
        record.setStatus("PENDING");
        record.setCreatorId(operatorId);
        record.setCreateTime(now);
        record.setDeleted(false);

        MetaColumn oldColumn = ddl.change().getOldColumn();
        MetaColumn newColumn = ddl.change().getNewColumn();

        if (oldColumn != null) {
            record.setColumnCode(oldColumn.getColumnCode());
        } else if (newColumn != null) {
            record.setColumnCode(newColumn.getColumnCode());
        }
        if (newColumn != null && oldColumn != null) {
            record.setOldColumnCode(oldColumn.getColumnCode());
        }

        record.setOldType(ddl.change().getOldType());
        record.setNewType(ddl.change().getNewType());
        record.setOldDefault(ddl.change().getOldDefault());
        record.setNewDefault(ddl.change().getNewDefault());

        return record;
    }
}
