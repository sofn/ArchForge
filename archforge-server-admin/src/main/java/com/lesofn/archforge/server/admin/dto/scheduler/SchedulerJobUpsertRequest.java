package com.lesofn.archforge.server.admin.dto.scheduler;

import lombok.Data;

/** Create/update payload for a scheduled job. */
@Data
public class SchedulerJobUpsertRequest {
    private String jobName;
    private String jobGroup;
    private String description;
    private String beanName;
    private String methodName;
    private String methodParams;
    private String cron;
    private Short misfirePolicy;
    private Boolean concurrent;
    private Short status;
}
