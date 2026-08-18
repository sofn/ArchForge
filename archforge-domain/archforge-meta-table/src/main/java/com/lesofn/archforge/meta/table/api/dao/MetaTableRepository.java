package com.lesofn.archforge.meta.table.api.dao;

import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MetaTableRepository extends JpaRepository<MetaTable, Long>, JpaSpecificationExecutor<MetaTable> {

    Optional<MetaTable> findByTableCodeAndDeletedFalse(String tableCode);

    boolean existsByTableCodeAndDeletedFalse(String tableCode);
}
