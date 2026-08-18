package com.lesofn.archforge.meta.table.api.service;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 元表格定义管理服务。
 */
public interface MetaTableAdminService {

    Long create(MetaTable table, List<MetaColumn> columns);

    void update(Long id, MetaTable table, List<MetaColumn> columns, Long operatorId);

    Long copy(Long id);

    MetaTable findById(Long id);

    List<MetaColumn> findColumns(Long tableId);

    Page<MetaTable> list(String keyword, Pageable pageable);

    long checkDelete(Long id);

    void delete(Long id, boolean force);
}
