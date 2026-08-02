package com.lesofn.archforge.demo.task.dto;

import lombok.Data;

/** Task list/item response DTO */
@Data
public class TaskResponse {

    private Long id;

    private String title;

    private String description;

    private String status;

    private String statusLabel;

    private Long uid;

    private Long createTime;
}
