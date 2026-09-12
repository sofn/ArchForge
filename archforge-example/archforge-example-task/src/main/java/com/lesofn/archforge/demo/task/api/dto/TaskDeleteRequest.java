package com.lesofn.archforge.demo.task.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Delete task request */
@Data
@SuppressWarnings("NullAway.Init") // Jackson/JPA-populated DTO
public class TaskDeleteRequest {

    @NotNull(message = "任务ID不能为空")
    private Long id;
}
