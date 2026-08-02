package com.lesofn.archforge.demo.task.dto;

import lombok.Data;

/** Task list query request */
@Data
public class TaskListRequest {

    private Integer currentPage = 1;

    private Integer pageSize = 10;

    private String title;

    private String status;

    private Long uid;
}
