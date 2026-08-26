package com.lesofn.archforge.server.admin.dto.request;

import java.util.Map;
import lombok.Data;

/**
 * 元表格数据列表查询请求
 */
@Data
public class MetaDataListRequest {

    /** 搜索过滤条件（字段编码 -> 值） */
    private Map<String, Object> filters;

    /** 当前页码（从1开始） */
    private Integer currentPage = 1;

    /** 每页大小（服务端按 arch-forge.meta-table.max-page-size 截断） */
    private Integer pageSize = 10;

    /** 排序字段（字段编码或 id/create_time/update_time 等审计列），默认 id */
    private String orderBy;

    /** 排序方向 ASC/DESC，默认 DESC */
    private String orderDir;

    /** 为 true 时跳过 COUNT(*) 统计，返回 total = -1 表示未统计 */
    private Boolean skipCount;
}
