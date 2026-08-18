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

    /** 每页大小 */
    private Integer pageSize = 10;
}
