package com.lesofn.archforge.task.api.dto;

import lombok.Data;
import org.jspecify.annotations.Nullable;

/** Task list/item response */
@Data
@SuppressWarnings("NullAway.Init") // Jackson/JPA-populated DTO
public class TaskResponse {

    private Long id;

    private String title;

    private @Nullable String description;

    private String status;

    private String statusLabel;

    private Long uid;

    private @Nullable Long createTime;
}
