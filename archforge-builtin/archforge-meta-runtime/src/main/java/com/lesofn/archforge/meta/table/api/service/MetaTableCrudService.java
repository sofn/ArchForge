package com.lesofn.archforge.meta.table.api.service;

import com.lesofn.archforge.meta.table.api.dto.ImportResponse;
import com.lesofn.archforge.meta.table.api.dto.MetaDataQuery;
import com.lesofn.archforge.meta.table.api.dto.MetaPageResponse;
import com.lesofn.archforge.meta.table.api.enums.MetaDataFormat;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * 元表格行数据通用 CRUD 服务。表以 {@code tableCode} 寻址（稳定身份键，跨环境一致；
 * 定义经 {@link MetaDefinitionRegistry} 解析）。
 */
public interface MetaTableCrudService {

    Long insert(String tableCode, Map<String, Object> row, Long currentUid);

    Boolean update(String tableCode, Long dataId, Map<String, Object> row, Long currentUid);

    Boolean softDelete(String tableCode, Long dataId, Long currentUid);

    MetaPageResponse<Map<String, Object>> list(String tableCode, MetaDataQuery query);

    void export(String tableCode, MetaDataFormat format, OutputStream out);

    ImportResponse importData(String tableCode, MetaDataFormat format, InputStream in, Long currentUid);
}
