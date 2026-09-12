package com.lesofn.archforge.demo.task.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.jspecify.annotations.Nullable;

/** Create task request */
@Data
@SuppressWarnings("NullAway.Init") // Jackson/JPA-populated DTO
public class TaskCreateRequest {

    @NotBlank(message = "任务标题不能为空")
    private String title;

    private @Nullable String description;

    private @Nullable Long uid;
}
