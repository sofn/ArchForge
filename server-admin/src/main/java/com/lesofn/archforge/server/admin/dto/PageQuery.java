package com.lesofn.archforge.server.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用分页查询请求
 *
 * @author lesofn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageQuery {

    /** 当前页码（从1开始） */
    private Integer currentPage = 1;

    /** 每页大小 */
    private Integer pageSize = 10;
}
