package com.lesofn.archforge.user.api.domain;

import com.lesofn.archforge.common.repository.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * Admin-managed scheduled job metadata for the db-scheduler runtime. The {@code
 * ReflectionJobHandler} reads {@link #beanName}, {@link #methodName}, {@link #methodParams} from
 * this row to invoke the target method; the runtime schedule lives in db-scheduler's {@code
 * scheduled_tasks} table keyed by {@code admin-job-<id>}.
 *
 * <p>Historical note: this table (and its API surface) previously fronted Quartz; it was migrated
 * to db-scheduler keeping job metadata, while Quartz's eleven QRTZ_* tables were dropped.
 *
 * @author sofn
 */
@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(
        name = "sys_scheduled_job",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_sys_scheduled_job_name_group",
                columnNames = {
                        "job_name", "job_group"
                }))
@DynamicInsert
@DynamicUpdate
public class SysScheduledJob extends BaseEntity<SysScheduledJob> {

    /** Status: paused (schedule persisted as a disabled schedule). */
    public static final short STATUS_PAUSED = 1;

    /** Status: running. */
    public static final short STATUS_RUNNING = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobName;

    private String jobGroup;

    private String description;

    private String beanName;

    private String methodName;

    @Column(columnDefinition = "TEXT")
    private String methodParams;

    private String cron;

    private Short misfirePolicy;

    private Boolean concurrent;

    private Short status;

    public boolean isPaused() { return status != null && status == STATUS_PAUSED; }

    public boolean isRunning() { return status != null && status == STATUS_RUNNING; }

    public SysScheduledJob pause() {
        this.status = STATUS_PAUSED;
        return this;
    }

    public SysScheduledJob resume() {
        this.status = STATUS_RUNNING;
        return this;
    }
}
