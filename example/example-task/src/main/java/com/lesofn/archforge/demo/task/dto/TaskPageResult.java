package com.lesofn.archforge.demo.task.dto;

import java.util.List;
import lombok.Data;

/** Task paged result matching vue-pure-admin table format */
@Data
public class TaskPageResult<T> {

    private List<T> list;

    private long total;

    private int pageSize;

    private int currentPage;

    public static <T> TaskPageResult<T> of(List<T> list, long total, int pageSize, int currentPage) {
        TaskPageResult<T> result = new TaskPageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPageSize(pageSize);
        result.setCurrentPage(currentPage);
        return result;
    }
}
