package com.lesofn.archforge.meta.table.api.dto;

import java.util.List;
import lombok.Data;

/**
 * 元表格分页结果。
 *
 * @param <T> 数据类型
 */
@Data
public class MetaPageResult<T> {

    private List<T> list;

    private long total;

    private int pageSize;

    private int currentPage;

    public static <T> MetaPageResult<T> of(List<T> list, long total, int pageSize, int currentPage) {
        MetaPageResult<T> result = new MetaPageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPageSize(pageSize);
        result.setCurrentPage(currentPage);
        return result;
    }
}
