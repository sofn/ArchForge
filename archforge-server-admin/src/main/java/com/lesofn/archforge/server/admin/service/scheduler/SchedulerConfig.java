package com.lesofn.archforge.server.admin.service.scheduler;

import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTaskWithPersistentSchedule;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import java.time.Duration;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * db-scheduler runtime wiring (replaces the former Quartz SchedulerFactoryBean).
 *
 * <p>
 * Why db-scheduler: a single {@code scheduled_tasks} table replaces Quartz's eleven QRTZ_*
 * tables, cluster-safety is row-level optimistic locking instead of a database lock manager, and
 * the {@link RecurringTaskWithPersistentSchedule} pattern keeps each job's cron expression in the
 * instance data — so the admin CRUD can change schedules with one idempotent {@code reschedule}
 * call (no delete/recreate dance like Quartz JobKey).
 *
 * <p>
 * The scheduler talks to the primary (user_master) datasource — same database that holds the
 * {@code sys_scheduled_job} metadata, so one transaction can touch both. Executes via {@link
 * ReflectionJobHandler}.
 *
 * @author sofn
 */
@Slf4j
@Configuration
public class SchedulerConfig {

    /** Single recurring task; one instance per {@code sys_scheduled_job} row. */
    public static final String ADMIN_JOB_TASK_NAME = "admin-scheduler-job";

    public static final TaskDescriptor<JobInvocationData> ADMIN_JOB_TASK = TaskDescriptor.of(ADMIN_JOB_TASK_NAME,
            JobInvocationData.class);

    /** One-shot task for the "run now" button. */
    public static final String ADMIN_JOB_ONCE_TASK_NAME = "admin-scheduler-job-once";

    public static final TaskDescriptor<JobInvocationData> ADMIN_JOB_ONCE_TASK = TaskDescriptor.of(ADMIN_JOB_ONCE_TASK_NAME,
            JobInvocationData.class);

    @Bean(destroyMethod = "stop")
    public Scheduler dbScheduler(
            DataSource dataSource,
            ReflectionJobHandler handler,
            @Value("${arch-forge.scheduler.threads:5}") int threads) {

        RecurringTaskWithPersistentSchedule<JobInvocationData> recurring = Tasks.recurringWithPersistentSchedule(ADMIN_JOB_TASK)
                .execute((inst, ctx) -> handler.run(inst.getData()));

        OneTimeTask<JobInvocationData> once = Tasks.oneTime(ADMIN_JOB_ONCE_TASK)
                .execute((inst, ctx) -> handler.run(inst.getData()));

        Scheduler scheduler = Scheduler.create(dataSource, recurring, once)
                .threads(threads)
                .pollingInterval(Duration.ofSeconds(1))
                .build();
        scheduler.start();
        log.info("db-scheduler started: tasks={}, threads={}", ADMIN_JOB_TASK_NAME, threads);
        return scheduler;
    }
}
