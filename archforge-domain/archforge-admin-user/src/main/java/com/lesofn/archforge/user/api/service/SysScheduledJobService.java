package com.lesofn.archforge.user.api.service;

import com.lesofn.archforge.user.api.domain.SysJobLog;
import com.lesofn.archforge.user.api.domain.SysScheduledJob;
import com.lesofn.archforge.user.api.domain.query.SysScheduledJobQuery;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Admin CRUD for scheduled jobs: metadata rows in {@code sys_scheduled_job} kept in lockstep with
 * runtime instances via {@link com.lesofn.archforge.user.api.scheduler.SchedulerJobRuntime}.
 *
 * <p>
 * Semantics notes vs the former Quartz implementation:
 *
 * <ul>
 * <li>missed executions are always skipped (db-scheduler cron semantics); {@code misfirePolicy}
 * is kept as metadata for API compatibility
 * <li>one instance never runs concurrently with itself; {@code concurrent} is kept as metadata
 * <li>cron expressions use 6-field unix style with seconds; Quartz-style {@code ?} is accepted
 * and normalized to {@code *}
 * </ul>
 *
 * @author sofn
 */
public interface SysScheduledJobService {

    Page<SysScheduledJob> page(@Nullable SysScheduledJobQuery criteria, Pageable pageable);

    SysScheduledJob get(Long id);

    /** Validates a cron expression against the scheduler runtime. */
    boolean validateCron(String cron);

    Long add(SysScheduledJob input);

    void update(Long id, SysScheduledJob input);

    void delete(Long id);

    void pause(Long id);

    void resume(Long id);

    /** Triggers an immediate one-shot execution (in addition to the recurring schedule). */
    void runOnce(Long id);

    Page<SysJobLog> logPage(Long jobId, Pageable pageable);
}
