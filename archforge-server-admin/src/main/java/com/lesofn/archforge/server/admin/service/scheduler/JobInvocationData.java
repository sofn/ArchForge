package com.lesofn.archforge.server.admin.service.scheduler;

import com.github.kagkarlsson.scheduler.task.TaskInstanceId;
import com.github.kagkarlsson.scheduler.task.helper.ScheduleAndData;
import com.github.kagkarlsson.scheduler.task.schedule.Schedule;
import java.io.Serializable;

/**
 * Persistent payload for admin-managed recurring jobs (db-scheduler).
 *
 * <p>
 * The whole record — including the {@link Schedule} — is persisted in the {@code
 * scheduled_tasks.task_data} column, so changing a cron expression or pausing a job is a single
 * {@code reschedule} that writes a new schedule. Executions resolve the target Spring bean by
 * reflection: {@code beanName#methodName(methodParams)}.
 *
 * <p>
 * Instance-id convention: {@code job-<sys_scheduled_job.id>} for recurring jobs; {@code
 * job-<id>-<uuid>} for one-shot manual runs.
 *
 * @author sofn
 */
public record JobInvocationData(
        Long jobId,
        String jobName,
        String jobGroup,
        String beanName,
        String methodName,
        String methodParams,
        Schedule schedule)
        implements
            ScheduleAndData,
            Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Schedule getSchedule() { return schedule; }

    @Override
    public Object getData() { return this; }

    /** The db-scheduler instance-id for the recurring instance of a job row. */
    public static String recurringInstanceId(Long jobId) {
        return "job-" + jobId;
    }

    /** Parses {@code job-<id>...} back into the job id, or null if malformed. */
    public static Long parseJobId(TaskInstanceId instance) {
        String id = instance.getId();
        if (id == null || !id.startsWith("job-")) {
            return null;
        }
        int end = id.indexOf('-', 4);
        try {
            return Long.parseLong(end > 0 ? id.substring(4, end) : id.substring(4));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
