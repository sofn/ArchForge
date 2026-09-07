package com.lesofn.archforge.server.admin.service.scheduler;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.SchedulableInstance;
import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.schedule.Schedule;
import com.github.kagkarlsson.scheduler.task.schedule.Schedules;
import com.lesofn.archforge.common.error.system.SystemException;
import com.lesofn.archforge.common.utils.jackson.JsonUtil;
import com.lesofn.archforge.common.utils.query.QueryHelp;
import com.lesofn.archforge.server.admin.dto.scheduler.SchedulerJobQueryRequest;
import com.lesofn.archforge.user.api.dao.SysJobLogRepository;
import com.lesofn.archforge.user.api.dao.SysScheduledJobRepository;
import com.lesofn.archforge.user.api.domain.SysJobLog;
import com.lesofn.archforge.user.api.domain.SysScheduledJob;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin CRUD for db-scheduler-backed jobs: metadata rows in {@code sys_scheduled_job} kept in
 * lockstep with runtime instances in {@code scheduled_tasks}.
 *
 * <p>
 * Synchronization is idempotent — {@code scheduleIfNotExists} for creation, {@code reschedule}
 * for updates (a reschedule rewrites the persisted {@link Schedule} in the instance data), {@code
 * cancel} for deletion, and a {@link DisabledSchedule} as the persisted schedule for paused jobs.
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
@Slf4j
@Service
public class ScheduledJobService {

    private final SysScheduledJobRepository jobRepository;
    private final SysJobLogRepository logRepository;
    private final SchedulerClient schedulerClient;
    private final ApplicationContext applicationContext;
    private final Set<String> allowedJobBeans;

    public ScheduledJobService(
            SysScheduledJobRepository jobRepository,
            SysJobLogRepository logRepository,
            SchedulerClient schedulerClient,
            ApplicationContext applicationContext,
            @Value("${arch-forge.scheduler.allowed-job-beans:}") String allowedJobBeans) {
        this.jobRepository = jobRepository;
        this.logRepository = logRepository;
        this.schedulerClient = schedulerClient;
        this.applicationContext = applicationContext;
        this.allowedJobBeans = Arrays.stream(allowedJobBeans.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Transactional(readOnly = true)
    public Page<SysScheduledJob> page(SchedulerJobQueryRequest criteria, Pageable pageable) {
        SchedulerJobQueryRequest effective = criteria == null ? new SchedulerJobQueryRequest() : criteria;
        if (effective.getDeleted() == null) {
            effective.setDeleted(false);
        }
        return jobRepository.findAll(
                (root, q, cb) -> QueryHelp.getPredicate(root, effective, cb), pageable);
    }

    @Transactional(readOnly = true)
    public SysScheduledJob get(Long id) {
        return jobRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scheduled job not found: " + id));
    }

    /** Validates a cron expression. Public so the controller can expose it directly. */
    public boolean validateCron(String cron) {
        if (cron == null || cron.isBlank()) {
            return false;
        }
        try {
            Schedules.cron(normalizeCron(cron));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Quartz-style {@code ?} is accepted for backward compatibility, normalized to {@code *}. */
    public static String normalizeCron(String cron) {
        return cron == null ? null : cron.replace('?', '*');
    }

    @Transactional
    public Long add(SysScheduledJob input) {
        validateInput(input);
        if (jobRepository.existsByJobNameAndJobGroup(input.getJobName(), input.getJobGroup())) {
            throw new IllegalArgumentException("Scheduled job already exists: " + input.getJobName() + "." + input
                    .getJobGroup());
        }
        if (input.getStatus() == null) {
            input.setStatus(SysScheduledJob.STATUS_PAUSED);
        }
        if (input.getMisfirePolicy() == null) {
            input.setMisfirePolicy((short) 1);
        }
        if (input.getConcurrent() == null) {
            input.setConcurrent(false);
        }
        input.setCron(normalizeCron(input.getCron()));
        SysScheduledJob saved = jobRepository.save(input);
        syncSchedule(saved);
        return saved.getId();
    }

    @Transactional
    public void update(Long id, SysScheduledJob input) {
        SysScheduledJob existing = get(id);
        validateInput(input);
        existing.setJobName(input.getJobName());
        existing.setJobGroup(input.getJobGroup());
        existing.setDescription(input.getDescription());
        existing.setBeanName(input.getBeanName());
        existing.setMethodName(input.getMethodName());
        existing.setMethodParams(input.getMethodParams());
        existing.setCron(normalizeCron(input.getCron()));
        if (input.getMisfirePolicy() != null) {
            existing.setMisfirePolicy(input.getMisfirePolicy());
        }
        if (input.getConcurrent() != null) {
            existing.setConcurrent(input.getConcurrent());
        }
        SysScheduledJob saved = jobRepository.save(existing);
        // A paused job keeps its DisabledSchedule; a running job gets the (possibly new) cron.
        syncSchedule(saved);
    }

    @Transactional
    public void delete(Long id) {
        SysScheduledJob job = get(id);
        try {
            schedulerClient.cancel(
                    SchedulerConfig.ADMIN_JOB_TASK.instanceId(JobInvocationData.recurringInstanceId(id)));
        } catch (RuntimeException e) {
            log.warn("Failed to cancel scheduled instance for job {}", id, e);
        }
        job.setDeleted(true);
        jobRepository.save(job);
    }

    /**
     * Pausing removes the runtime instance (db-scheduler has no persisted "paused" state —
     * DisabledSchedule is read-only). The sys_scheduled_job row remains the source of truth:
     * {@code resume()} re-creates the instance from it. Execution history stats in
     * scheduled_tasks are not preserved across a pause, matching Quartz's pause semantics.
     */
    @Transactional
    public void pause(Long id) {
        SysScheduledJob job = get(id);
        cancelQuietly(job);
        job.pause();
        jobRepository.save(job);
    }

    @Transactional
    public void resume(Long id) {
        SysScheduledJob job = get(id);
        persistSchedule(job, Schedules.cron(normalizeCron(job.getCron())));
        job.resume();
        jobRepository.save(job);
    }

    /** Triggers an immediate one-shot execution (in addition to the recurring schedule). */
    public void runOnce(Long id) {
        SysScheduledJob job = get(id);
        JobInvocationData data = new JobInvocationData(job.getId(), job.getJobName(), job.getJobGroup(), job.getBeanName(), job
                .getMethodName(), job.getMethodParams(), null);
        schedulerClient.schedule(
                SchedulerConfig.ADMIN_JOB_ONCE_TASK
                        .instance(JobInvocationData.recurringInstanceId(id) + "-" + UUID.randomUUID())
                        .data(data)
                        .scheduledTo(Instant.now()));
    }

    @Transactional(readOnly = true)
    public Page<SysJobLog> logPage(Long jobId, Pageable pageable) {
        return logRepository.findByJobIdOrderByStartedAtDesc(jobId, pageable);
    }

    // ---------- scheduler wiring ----------

    /**
     * Idempotent upsert of the runtime instance: schedule if absent, reschedule otherwise.
     * Invoked after every admin mutation and at startup.
     */
    public void syncSchedule(SysScheduledJob job) {
        if (job.isRunning()) {
            persistSchedule(job, Schedules.cron(normalizeCron(job.getCron())));
        } else {
            cancelQuietly(job);
        }
    }

    private void persistSchedule(SysScheduledJob job, Schedule schedule) {
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

    private void cancelQuietly(SysScheduledJob job) {
        try {
            schedulerClient.cancel(
                    SchedulerConfig.ADMIN_JOB_TASK.instanceId(JobInvocationData.recurringInstanceId(job.getId())));
        } catch (RuntimeException e) {
            log.warn("Failed to cancel scheduled instance for job {}", job.getId(), e);
        }
    }

    private static JobInvocationData toData(SysScheduledJob job, Schedule schedule) {
        return new JobInvocationData(job.getId(), job.getJobName(), job.getJobGroup(), job.getBeanName(), job
                .getMethodName(), job.getMethodParams(), schedule);
    }

    /** Reflective dispatch is only permitted for allowlisted beans with a public declared method. */
    private void validateInput(SysScheduledJob input) {
        String beanName = input.getBeanName();
        String methodParams = input.getMethodParams();
        if (!allowedJobBeans.isEmpty() && !allowedJobBeans.contains(beanName)) {
            throw new SystemException("Scheduled job bean is not allowlisted: " + beanName);
        }
        if (!applicationContext.containsBean(beanName)) {
            throw new SystemException("Scheduled job bean does not exist: " + beanName);
        }
        int arity = parseArity(methodParams);
        if (findPublicDeclaredMethod(
                applicationContext.getBean(beanName).getClass(), input.getMethodName(), arity) == null) {
            throw new SystemException("Scheduled job method is not invocable: " + beanName + "#" + input.getMethodName());
        }
        if (!validateCron(input.getCron())) {
            throw new SystemException("Invalid cron expression: " + input.getCron());
        }
    }

    private static int parseArity(String methodParams) {
        if (methodParams == null || methodParams.isBlank()) {
            return 0;
        }
        List<Object> parsed = JsonUtil.fromList(methodParams, Object.class);
        return parsed == null ? 0 : parsed.size();
    }

    private static Method findPublicDeclaredMethod(Class<?> type, String name, int arity) {
        for (Method m : type.getMethods()) {
            boolean matches = m.getName().equals(name) && m.getParameterCount() == arity && Modifier.isPublic(m
                    .getModifiers()) && !m.isSynthetic() && m.getDeclaringClass() != Object.class && m.getDeclaringClass()
                            .isAssignableFrom(type);
            if (matches) {
                return m;
            }
        }
        return null;
    }
}
