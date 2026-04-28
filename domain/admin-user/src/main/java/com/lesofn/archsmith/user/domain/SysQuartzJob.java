package com.lesofn.archsmith.user.domain;

import com.lesofn.archsmith.common.repository.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * Quartz reflective job metadata. The {@code QuartzReflectionJob} reads {@link #beanName}, {@link
 * #methodName}, {@link #methodParams} from this row to invoke the target method.
 *
 * @author sofn
 */
@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(
        name = "sys_quartz_job",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_sys_quartz_job_name_group",
                        columnNames = {"job_name", "job_group"}))
@DynamicInsert
@DynamicUpdate
public class SysQuartzJob extends BaseEntity<SysQuartzJob> {

    /** Status: paused (no trigger fires). */
    public static final short STATUS_PAUSED = 1;

    /** Status: running (trigger active). */
    public static final short STATUS_RUNNING = 2;

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

    public boolean isPaused() {
        return status != null && status == STATUS_PAUSED;
    }

    public boolean isRunning() {
        return status != null && status == STATUS_RUNNING;
    }

    public SysQuartzJob pause() {
        this.status = STATUS_PAUSED;
        return this;
    }

    public SysQuartzJob resume() {
        this.status = STATUS_RUNNING;
        return this;
    }
}
