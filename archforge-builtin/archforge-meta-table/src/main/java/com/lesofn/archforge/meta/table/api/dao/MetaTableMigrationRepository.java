package com.lesofn.archforge.meta.table.api.dao;

import com.lesofn.archforge.meta.table.api.domain.MetaTableMigration;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 元表格 Schema 迁移记录仓库。
 */
public interface MetaTableMigrationRepository extends JpaRepository<MetaTableMigration, Long> {

    List<MetaTableMigration> findByTableIdAndDeletedFalseOrderByVersionAsc(Long tableId);
}
