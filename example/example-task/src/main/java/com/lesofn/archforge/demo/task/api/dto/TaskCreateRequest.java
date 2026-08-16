package com.lesofn.archforge.demo.task.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Create task request */
@Data
public class TaskCreateRequest {

    @NotBlank(message = "任务标题不能为空")
    private String title;

    private String description;

    private Long uid;
}
