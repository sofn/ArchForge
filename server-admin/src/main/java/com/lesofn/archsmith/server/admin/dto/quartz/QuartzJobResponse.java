package com.lesofn.archsmith.server.admin.dto.quartz;

import com.lesofn.archsmith.user.domain.SysQuartzJob;
import java.time.LocalDateTime;
import lombok.Data;

/** Response DTO mirroring {@link SysQuartzJob} for the admin list view. */
@Data
public class QuartzJobResponse {
    private Long id;
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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static QuartzJobResponse from(SysQuartzJob j) {
        QuartzJobResponse r = new QuartzJobResponse();
        r.setId(j.getId());
        r.setJobName(j.getJobName());
        r.setJobGroup(j.getJobGroup());
        r.setDescription(j.getDescription());
        r.setBeanName(j.getBeanName());
        r.setMethodName(j.getMethodName());
        r.setMethodParams(j.getMethodParams());
        r.setCron(j.getCron());
        r.setMisfirePolicy(j.getMisfirePolicy());
        r.setConcurrent(j.getConcurrent());
        r.setStatus(j.getStatus());
        r.setCreateTime(j.getCreateTime());
        r.setUpdateTime(j.getUpdateTime());
        return r;
    }
}
