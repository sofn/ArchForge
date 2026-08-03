package com.lesofn.archforge.server.admin.dto.request;

import lombok.Data;

/**
 * 元表格列表查询请求
 */
@Data
public class MetaTableListRequest {

    /** 搜索关键字（表格编码/名称） */
    private String keyword;

    /** 当前页码（从1开始） */
    private Integer currentPage = 1;

    /** 每页大小 */
    private Integer pageSize = 10;
}
