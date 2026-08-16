package com.lesofn.archforge.meta.table.api.dto;

import java.util.List;
import lombok.Data;

/**
 * 元表格分页结果。
 *
 * @param <T> 数据类型
 */
@Data
public class MetaPageResponse<T> {

    private List<T> list;

    private long total;

    private int pageSize;

    private int currentPage;

    public static <T> MetaPageResponse<T> of(List<T> list, long total, int pageSize, int currentPage) {
        MetaPageResponse<T> result = new MetaPageResponse<>();
        result.setList(list);
        result.setTotal(total);
        result.setPageSize(pageSize);
        result.setCurrentPage(currentPage);
        return result;
    }
}
