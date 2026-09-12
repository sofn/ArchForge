package com.lesofn.archforge.demo.task.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

/** Update task request */
@Data
@SuppressWarnings("NullAway.Init") // Jackson/JPA-populated DTO
public class TaskUpdateRequest {

    @NotNull(message = "任务ID不能为空")
    private Long id;

    @NotBlank(message = "任务标题不能为空")
    private String title;

    private @Nullable String description;

    private @Nullable Long uid;
}
