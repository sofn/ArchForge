package com.lesofn.archforge.user.api.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Per-execution audit log of a {@link SysScheduledJob}. Persisted by the reflective job runner
 * with status, duration, and any error message.
 *
 * @author sofn
 */
@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(
        name = "sys_job_log",
        indexes = {
                @Index(name = "idx_sys_job_log_job_id", columnList = "job_id"),
                @Index(name = "idx_sys_job_log_started_at", columnList = "started_at")
        })
public class SysJobLog {

    /** Status: execution succeeded. */
    public static final short STATUS_SUCCESS = 0;

    /** Status: execution failed. */
    public static final short STATUS_FAILURE = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long jobId;

    private String jobName;

    private String jobGroup;

    private String beanName;

    private String methodName;

    @Column(columnDefinition = "TEXT")
    private String methodParams;

    /** 0 = success, 1 = failure. */
    private Short status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Long durationMs;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    public static SysJobLog success(SysScheduledJob job, String methodParams, long durationMs,
            LocalDateTime startedAt, LocalDateTime finishedAt) {
        return new SysJobLog()
                .setJobId(job.getId())
                .setJobName(job.getJobName())
                .setJobGroup(job.getJobGroup())
                .setBeanName(job.getBeanName())
                .setMethodName(job.getMethodName())
                .setMethodParams(methodParams)
                .setStatus(STATUS_SUCCESS)
                .setDurationMs(durationMs)
                .setStartedAt(startedAt)
                .setFinishedAt(finishedAt);
    }

    public static SysJobLog failure(SysScheduledJob job, String methodParams, String errorMessage,
            long durationMs, LocalDateTime startedAt, LocalDateTime finishedAt) {
        return new SysJobLog()
                .setJobId(job.getId())
                .setJobName(job.getJobName())
                .setJobGroup(job.getJobGroup())
                .setBeanName(job.getBeanName())
                .setMethodName(job.getMethodName())
                .setMethodParams(methodParams)
                .setStatus(STATUS_FAILURE)
                .setErrorMessage(errorMessage)
                .setDurationMs(durationMs)
                .setStartedAt(startedAt)
                .setFinishedAt(finishedAt);
    }

    public boolean isSuccess() { return status != null && status == STATUS_SUCCESS; }
}
