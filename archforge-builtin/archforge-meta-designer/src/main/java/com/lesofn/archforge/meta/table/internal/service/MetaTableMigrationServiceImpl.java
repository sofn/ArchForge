package com.lesofn.archforge.meta.table.internal.service;

import com.lesofn.archforge.meta.table.internal.dao.MetaTableMigrationRepository;
import com.lesofn.archforge.meta.table.api.domain.MetaTableMigration;
import com.lesofn.archforge.meta.table.api.service.MetaTableMigrationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 元表格 Schema 迁移记录服务。
 */
@Service
@RequiredArgsConstructor
public class MetaTableMigrationServiceImpl implements MetaTableMigrationService {

    private final MetaTableMigrationRepository migrationRepository;

    @Override
    public List<MetaTableMigration> listByTableId(Long tableId) {
        return migrationRepository.findByTableIdAndDeletedFalseOrderByVersionAsc(tableId);
    }

    @Override
    @Transactional
    public List<MetaTableMigration> saveAll(List<MetaTableMigration> records) {
        return migrationRepository.saveAll(records);
    }
}
