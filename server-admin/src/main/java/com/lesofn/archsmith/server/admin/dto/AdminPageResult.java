package com.lesofn.archsmith.server.admin.dto;

import java.util.List;
import lombok.Data;

/**
 * 管理端分页结果DTO，匹配vue-pure-admin前端分页格式
 *
 * @author lesofn
 */
@Data
public class AdminPageResult<T> {

    /** 数据列表 */
    private List<T> list;

    /** 总记录数 */
    private long total;

    /** 每页大小 */
    private int pageSize;

    /** 当前页码（从1开始） */
    private int currentPage;

    public static <T> AdminPageResult<T> of(
            List<T> list, long total, int pageSize, int currentPage) {
        AdminPageResult<T> result = new AdminPageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPageSize(pageSize);
        result.setCurrentPage(currentPage);
        return result;
    }
}
