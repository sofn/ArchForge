package com.lesofn.archforge.server.admin.dto.scheduler;

import com.lesofn.archforge.user.api.domain.SysJobLog;
import java.time.LocalDateTime;
import lombok.Data;

/** Response DTO mirroring {@link SysJobLog}. */
@Data
public class SchedulerLogResponse {
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

    public static SchedulerLogResponse from(SysJobLog l) {
        SchedulerLogResponse r = new SchedulerLogResponse();
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
