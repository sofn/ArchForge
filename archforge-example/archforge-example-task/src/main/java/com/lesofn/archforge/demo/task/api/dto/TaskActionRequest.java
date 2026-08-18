package com.lesofn.archforge.demo.task.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Task status action request (start/complete/cancel) */
@Data
public class TaskActionRequest {

    @NotNull(message = "任务ID不能为空")
    private Long id;
}
