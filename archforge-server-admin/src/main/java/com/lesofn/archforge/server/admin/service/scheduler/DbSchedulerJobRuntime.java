package com.lesofn.archforge.server.admin.service.scheduler;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.SchedulableInstance;
import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.schedule.Schedule;
import com.github.kagkarlsson.scheduler.task.schedule.Schedules;
import com.lesofn.archforge.user.api.domain.SysScheduledJob;
import com.lesofn.archforge.user.api.scheduler.SchedulerJobRuntime;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * db-scheduler adapter of the {@link SchedulerJobRuntime} port: {@code scheduleIfNotExists} for
 * creation, {@code reschedule} for updates, {@code cancel} for deletion, and a one-shot instance
 * for immediate runs.
 *
 * @author sofn
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DbSchedulerJobRuntime implements SchedulerJobRuntime {

    private final SchedulerClient schedulerClient;

    @Override
    public void syncSchedule(SysScheduledJob job) {
        if (job.isRunning()) {
            persistSchedule(job);
        } else {
            cancelSchedule(job);
        }
    }

    @Override
    public void persistSchedule(SysScheduledJob job) {
        Schedule schedule = Schedules.cron(SchedulerJobRuntime.normalizeCron(job.getCron()));
        TaskDescriptor<JobInvocationData> task = SchedulerConfig.ADMIN_JOB_TASK;
        SchedulableInstance<JobInvocationData> schedulable = task.instance(JobInvocationData.recurringInstanceId(job.getId()))
                .data(toData(job, schedule))
                .scheduledAccordingToData();

        if (schedulerClient.getScheduledExecution(
                task.instanceId(JobInvocationData.recurringInstanceId(job.getId())))
                .isPresent()) {
            schedulerClient.reschedule(schedulable);
        } else {
            schedulerClient.scheduleIfNotExists(schedulable);
        }
    }

    @Override
    public void cancelSchedule(SysScheduledJob job) {
        try {
            schedulerClient.cancel(
                    SchedulerConfig.ADMIN_JOB_TASK.instanceId(JobInvocationData.recurringInstanceId(job.getId())));
        } catch (RuntimeException e) {
            log.warn("Failed to cancel scheduled instance for job {}", job.getId(), e);
        }
    }

    @Override
    public void triggerOnce(SysScheduledJob job) {
        JobInvocationData data = toData(job, Schedules.cron(SchedulerJobRuntime.normalizeCron(job.getCron())));
        schedulerClient.schedule(
                SchedulerConfig.ADMIN_JOB_ONCE_TASK
                        .instance(JobInvocationData.recurringInstanceId(job.getId()) + "-" + UUID.randomUUID())
                        .data(data)
                        .scheduledTo(Instant.now()));
    }

    @Override
    public boolean isValidCron(String cron) {
        try {
            Schedules.cron(SchedulerJobRuntime.normalizeCron(cron));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static JobInvocationData toData(SysScheduledJob job, Schedule schedule) {
        return new JobInvocationData(job.getId(), job.getJobName(), job.getJobGroup(), job.getBeanName(), job
                .getMethodName(), job.getMethodParams(), schedule);
    }
}
