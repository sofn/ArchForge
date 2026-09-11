package com.lesofn.archforge.meta.table.api.service;

import com.lesofn.archforge.meta.table.api.dao.MetaTableMigrationRepository;
import com.lesofn.archforge.meta.table.api.domain.MetaTableMigration;
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

    public List<MetaTableMigration> listByTableId(Long tableId) {
        return migrationRepository.findByTableIdAndDeletedFalseOrderByVersionAsc(tableId);
    }

    @Transactional("metaTableTransactionManager")
    public List<MetaTableMigration> saveAll(List<MetaTableMigration> records) {
        return migrationRepository.saveAll(records);
    }
}
