package com.lesofn.archforge.demo.task.api.dto;

import lombok.Data;
import org.jspecify.annotations.Nullable;

/** Task list query request */
@Data
@SuppressWarnings("NullAway.Init") // Jackson/JPA-populated DTO
public class TaskListRequest {

    private Integer currentPage = 1;

    private Integer pageSize = 10;

    private @Nullable String title;

    private @Nullable String status;

    private @Nullable Long uid;
}
