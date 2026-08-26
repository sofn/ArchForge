package com.lesofn.archforge.meta.table.api.dto;

import java.util.Map;

/**
 * 元表格数据行查询请求。
 *
 * @param filters 过滤条件（字段编码 -> 值）
 * @param currentPage 当前页码（从 1 开始）
 * @param pageSize 每页大小（调用方负责按配置上限截断）
 * @param orderBy 排序字段：字段编码或审计列（id/creator_id/create_time/updater_id/update_time），默认 id
 * @param orderDir 排序方向 ASC/DESC，默认 DESC
 * @param skipCount 为 true 时跳过 COUNT(*) 统计，返回 total = -1（未知）
 */
public record MetaDataQuery(
        Map<String, Object> filters,
        int currentPage,
        int pageSize,
        String orderBy,
        String orderDir,
        boolean skipCount) {

    public static MetaDataQuery of(Map<String, Object> filters, int currentPage, int pageSize) {
        return new MetaDataQuery(filters, currentPage, pageSize, null, null, false);
    }
}
