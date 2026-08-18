package com.lesofn.archforge.demo.task.api.dto;

import java.util.List;
import lombok.Data;

/** Task paged result matching vue-pure-admin table format */
@Data
public class TaskPageResponse<T> {

    private List<T> list;

    private long total;

    private int pageSize;

    private int currentPage;

    public static <T> TaskPageResponse<T> of(List<T> list, long total, int pageSize, int currentPage) {
        TaskPageResponse<T> result = new TaskPageResponse<>();
        result.setList(list);
        result.setTotal(total);
        result.setPageSize(pageSize);
        result.setCurrentPage(currentPage);
        return result;
    }
}
