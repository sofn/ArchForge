package com.lesofn.archforge.user.api.scheduler;

import com.lesofn.archforge.user.api.domain.SysScheduledJob;
import org.jspecify.annotations.Nullable;

/**
 * Port for the scheduler runtime that keeps {@code scheduled_tasks} instances in lockstep with
 * {@code sys_scheduled_job} metadata rows. Implemented by the hosting application (db-scheduler
 * adapter); absent in contexts that embed no scheduler.
 *
 * <p>
 * Synchronization is idempotent — schedule if absent, reschedule otherwise; a reschedule rewrites
 * the persisted schedule in the instance data. Pausing removes the runtime instance (the row
 * remains the source of truth; resume re-creates it).
 *
 * @author sofn
 */
public interface SchedulerJobRuntime {

    /**
     * Idempotent upsert of the runtime instance: schedule the persisted cron if the job is running,
     * cancel the instance otherwise. Invoked after every admin mutation and at startup.
     */
    void syncSchedule(SysScheduledJob job);

    /** Cancels the runtime instance for the job, tolerating an absent instance. */
    void cancelSchedule(SysScheduledJob job);

    /**
     * Persists a runtime instance with the job's cron regardless of its current status — used by
     * {@code resume()} before the row flips to running.
     */
    void persistSchedule(SysScheduledJob job);

    /** Triggers an immediate one-shot execution (in addition to the recurring schedule). */
    void triggerOnce(SysScheduledJob job);

    /** Whether the runtime can parse the cron expression. */
    boolean isValidCron(String cron);

    /** Quartz-style {@code ?} is accepted for backward compatibility, normalized to {@code *}. */
    @Nullable
    static String normalizeCron(@Nullable String cron) {
        return cron == null ? null : cron.replace('?', '*');
    }
}
