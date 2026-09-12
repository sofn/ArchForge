package com.lesofn.archforge.demo.task.api.dto;

import lombok.Data;
import org.jspecify.annotations.Nullable;

/** Task list/item response DTO */
@Data
@SuppressWarnings("NullAway.Init") // Jackson/JPA-populated DTO
public class TaskDTO {

    private Long id;

    private String title;

    private @Nullable String description;

    private String status;

    private String statusLabel;

    private Long uid;

    private @Nullable Long createTime;
}
