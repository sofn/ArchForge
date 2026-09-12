package com.lesofn.archforge.server.admin.service.scheduler;

import com.github.kagkarlsson.scheduler.ScheduledExecution;
import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.lesofn.archforge.user.api.dao.SysScheduledJobRepository;
import com.lesofn.archforge.user.api.scheduler.SchedulerJobRuntime;
import com.lesofn.archforge.user.api.domain.SysScheduledJob;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Reconciles {@code scheduled_tasks} with {@code sys_scheduled_job} after startup: every
 * non-deleted row gets its schedule re-asserted (idempotent), and runtime instances whose job row
 * is gone are cancelled.
 *
 * <p>
 * This is what makes admin edits survive restarts and keeps the two stores from drifting —
 * the equivalent of Quartz's {@code overwrite-existing-jobs: true} plus a garbage collection pass.
 *
 * @author sofn
 */
@Slf4j
@Component
public class SchedulerStartupSync {

    private final SysScheduledJobRepository jobRepository;
    private final SchedulerClient schedulerClient;
    private final SchedulerJobRuntime jobRuntime;

    public SchedulerStartupSync(
            SysScheduledJobRepository jobRepository,
            SchedulerClient schedulerClient,
            SchedulerJobRuntime jobRuntime) {
        this.jobRepository = jobRepository;
        this.schedulerClient = schedulerClient;
        this.jobRuntime = jobRuntime;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncAll() {
        int synced = 0;
        for (SysScheduledJob job : jobRepository.findAll()) {
            if (Boolean.TRUE.equals(job.getDeleted())) {
                continue;
            }
            jobRuntime.syncSchedule(job);
            synced++;
        }

        List<ScheduledExecution<Object>> instances = schedulerClient.getScheduledExecutionsForTask(
                SchedulerConfig.ADMIN_JOB_TASK_NAME);
        int cancelled = 0;
        for (ScheduledExecution<Object> execution : instances) {
            Long jobId = JobInvocationData.parseJobId(execution.getTaskInstance());
            boolean known = jobId != null && jobRepository.findById(jobId)
                    .map(j -> !Boolean.TRUE.equals(j.getDeleted()))
                    .orElse(false);
            if (!known) {
                schedulerClient.cancel(execution.getTaskInstance());
                cancelled++;
            }
        }
        log.info("Scheduler startup sync: {} jobs reconciled, {} orphan instances cancelled", synced, cancelled);
    }
}
