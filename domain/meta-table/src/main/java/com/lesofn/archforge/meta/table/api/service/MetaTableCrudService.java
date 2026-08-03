package com.lesofn.archforge.meta.table.api.service;

import com.lesofn.archforge.meta.table.api.dto.MetaPageResult;
import java.io.OutputStream;
import java.util.Map;

/**
 * 元表格行数据通用 CRUD 服务。
 */
public interface MetaTableCrudService {

    Long insert(Long tableId, Map<String, Object> row, Long currentUid);

    Boolean update(Long tableId, Long dataId, Map<String, Object> row, Long currentUid);

    Boolean softDelete(Long tableId, Long dataId, Long currentUid);

    MetaPageResult<Map<String, Object>> list(Long tableId, Map<String, Object> filters, int currentPage, int pageSize);

    void export(Long tableId, OutputStream out);
}
