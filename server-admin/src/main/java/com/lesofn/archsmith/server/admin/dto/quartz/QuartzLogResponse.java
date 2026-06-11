package com.lesofn.archsmith.server.admin.dto.quartz;

import com.lesofn.archsmith.user.domain.SysQuartzLog;
import java.time.LocalDateTime;
import lombok.Data;

/** Response DTO for execution log entries. */
@Data
public class QuartzLogResponse {
    private Long id;
    private Long jobId;
    private String jobName;
    private String jobGroup;
    private String beanName;
    private String methodName;
    private String methodParams;
    private Short status;
    private String errorMessage;
    private Long durationMs;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    public static QuartzLogResponse from(SysQuartzLog l) {
        QuartzLogResponse r = new QuartzLogResponse();
        r.setId(l.getId());
        r.setJobId(l.getJobId());
        r.setJobName(l.getJobName());
        r.setJobGroup(l.getJobGroup());
        r.setBeanName(l.getBeanName());
        r.setMethodName(l.getMethodName());
        r.setMethodParams(l.getMethodParams());
        r.setStatus(l.getStatus());
        r.setErrorMessage(l.getErrorMessage());
        r.setDurationMs(l.getDurationMs());
        r.setStartedAt(l.getStartedAt());
        r.setFinishedAt(l.getFinishedAt());
        return r;
    }
}
