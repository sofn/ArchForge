package com.lesofn.archforge.meta.table.api.service;

import com.lesofn.archforge.meta.table.api.domain.MetaTableMigration;
import java.util.List;

/**
 * 元表格 Schema 迁移记录服务。
 */
public interface MetaTableMigrationService {

    List<MetaTableMigration> listByTableId(Long tableId);

    List<MetaTableMigration> saveAll(List<MetaTableMigration> records);
}
