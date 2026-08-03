package com.lesofn.archforge.meta.table.api.dao;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetaColumnRepository extends JpaRepository<MetaColumn, Long> {

    List<MetaColumn> findByTableIdAndDeletedFalseOrderBySortAsc(Long tableId);

    List<MetaColumn> findByTableIdAndDeletedFalseAndListVisibleTrueOrderBySortAsc(Long tableId);

    List<MetaColumn> findByTableIdAndDeletedFalseAndSearchableTrueOrderBySortAsc(Long tableId);

    long countByTableIdAndDeletedFalse(Long tableId);

    void deleteByTableId(Long tableId);
}
