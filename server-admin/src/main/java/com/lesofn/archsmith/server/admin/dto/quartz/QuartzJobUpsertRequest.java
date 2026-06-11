package com.lesofn.archsmith.server.admin.dto.quartz;

import lombok.Data;

/** Create/update payload for Quartz jobs. */
@Data
public class QuartzJobUpsertRequest {
    private String jobName;
    private String jobGroup;
    private String description;
    private String beanName;
    private String methodName;
    private String methodParams;
    private String cron;
    private Short misfirePolicy;
    private Boolean concurrent;
}
