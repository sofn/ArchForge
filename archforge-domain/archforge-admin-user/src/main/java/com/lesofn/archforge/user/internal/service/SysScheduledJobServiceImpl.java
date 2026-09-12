package com.lesofn.archforge.user.internal.service;

import com.lesofn.archforge.common.error.system.SystemException;
import com.lesofn.archforge.common.utils.jackson.JsonUtil;
import com.lesofn.archforge.common.utils.query.QueryHelp;
import com.lesofn.archforge.user.api.dao.SysJobLogRepository;
import com.lesofn.archforge.user.api.dao.SysScheduledJobRepository;
import com.lesofn.archforge.user.api.domain.SysJobLog;
import com.lesofn.archforge.user.api.domain.SysScheduledJob;
import com.lesofn.archforge.user.api.domain.query.SysScheduledJobQuery;
import com.lesofn.archforge.user.api.scheduler.SchedulerJobRuntime;
import com.lesofn.archforge.user.api.service.SysScheduledJobService;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Domain side of the db-scheduler job platform: owns {@code sys_scheduled_job} metadata rules and
 * delegates runtime-instance operations to the {@link SchedulerJobRuntime} port.
 *
 * <p>
 * The port is resolved lazily via {@link ObjectProvider}: contexts that scan this bean without a
 * scheduler runtime (e.g. server-web) still get working read methods, while mutating operations
 * fail explicitly.
 *
 * @author sofn
 */
@Service
public class SysScheduledJobServiceImpl implements SysScheduledJobService {

    private final SysScheduledJobRepository jobRepository;
    private final SysJobLogRepository logRepository;
    private final ObjectProvider<SchedulerJobRuntime> runtime;
    private final ApplicationContext applicationContext;
    private final Set<String> allowedJobBeans;

    public SysScheduledJobServiceImpl(
            SysScheduledJobRepository jobRepository,
            SysJobLogRepository logRepository,
            ObjectProvider<SchedulerJobRuntime> runtime,
            ApplicationContext applicationContext,
            @Value("${arch-forge.scheduler.allowed-job-beans:}") String allowedJobBeans) {
        this.jobRepository = jobRepository;
        this.logRepository = logRepository;
        this.runtime = runtime;
        this.applicationContext = applicationContext;
        this.allowedJobBeans = Arrays.stream(allowedJobBeans.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SysScheduledJob> page(@Nullable SysScheduledJobQuery criteria, Pageable pageable) {
        SysScheduledJobQuery effective = criteria == null ? new SysScheduledJobQuery() : criteria;
        if (effective.getDeleted() == null) {
            effective.setDeleted(false);
        }
        return jobRepository.findAll(
                (root, q, cb) -> QueryHelp.getPredicate(root, effective, cb), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public SysScheduledJob get(Long id) {
        return jobRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scheduled job not found: " + id));
    }

    @Override
    public boolean validateCron(@Nullable String cron) {
        if (cron == null || cron.isBlank()) {
            return false;
        }
        return runtime().isValidCron(cron);
    }

    @Override
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
        input.setCron(SchedulerJobRuntime.normalizeCron(input.getCron()));
        SysScheduledJob saved = jobRepository.save(input);
        runtime().syncSchedule(saved);
        return saved.getId();
    }

    @Override
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
        existing.setCron(SchedulerJobRuntime.normalizeCron(input.getCron()));
        if (input.getMisfirePolicy() != null) {
            existing.setMisfirePolicy(input.getMisfirePolicy());
        }
        if (input.getConcurrent() != null) {
            existing.setConcurrent(input.getConcurrent());
        }
        SysScheduledJob saved = jobRepository.save(existing);
        // A paused job keeps its DisabledSchedule; a running job gets the (possibly new) cron.
        runtime().syncSchedule(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysScheduledJob job = get(id);
        runtime().cancelSchedule(job);
        job.setDeleted(true);
        jobRepository.save(job);
    }

    /**
     * Pausing removes the runtime instance (db-scheduler has no persisted "paused" state —
     * DisabledSchedule is read-only). The sys_scheduled_job row remains the source of truth:
     * {@code resume()} re-creates the instance from it. Execution history stats in
     * scheduled_tasks are not preserved across a pause, matching Quartz's pause semantics.
     */
    @Override
    @Transactional
    public void pause(Long id) {
        SysScheduledJob job = get(id);
        runtime().cancelSchedule(job);
        job.pause();
        jobRepository.save(job);
    }

    @Override
    @Transactional
    public void resume(Long id) {
        SysScheduledJob job = get(id);
        runtime().persistSchedule(job);
        job.resume();
        jobRepository.save(job);
    }

    @Override
    public void runOnce(Long id) {
        runtime().triggerOnce(get(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SysJobLog> logPage(Long jobId, Pageable pageable) {
        return logRepository.findByJobIdOrderByStartedAtDesc(jobId, pageable);
    }

    private SchedulerJobRuntime runtime() {
        return runtime.getIfAvailable(() -> {
            throw new SystemException("Scheduler runtime is not available in this application");
        });
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

    private static int parseArity(@Nullable String methodParams) {
        if (methodParams == null || methodParams.isBlank()) {
            return 0;
        }
        List<Object> parsed = JsonUtil.fromList(methodParams, Object.class);
        return parsed == null ? 0 : parsed.size();
    }

    private static @Nullable Method findPublicDeclaredMethod(Class<?> type, String name, int arity) {
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
