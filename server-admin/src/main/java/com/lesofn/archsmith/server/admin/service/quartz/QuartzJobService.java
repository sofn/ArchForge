package com.lesofn.archsmith.server.admin.service.quartz;

import com.lesofn.archsmith.user.dao.SysQuartzJobRepository;
import com.lesofn.archsmith.user.dao.SysQuartzLogRepository;
import com.lesofn.archsmith.user.domain.SysQuartzJob;
import com.lesofn.archsmith.user.domain.SysQuartzLog;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD + Quartz scheduler operations for {@link SysQuartzJob}. Persists metadata in {@code
 * sys_quartz_job} and registers/updates a corresponding Quartz {@link JobDetail} + cron trigger.
 *
 * @author sofn
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuartzJobService {

    private final SysQuartzJobRepository jobRepository;
    private final SysQuartzLogRepository logRepository;
    private final Scheduler scheduler;

    @Transactional(readOnly = true)
    public Page<SysQuartzJob> page(String jobName, Short status, Pageable pageable) {
        Specification<SysQuartzJob> spec =
                (root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(cb.isFalse(root.get("deleted")));
                    if (jobName != null && !jobName.isBlank()) {
                        predicates.add(cb.like(root.get("jobName"), "%" + jobName + "%"));
                    }
                    if (status != null) {
                        predicates.add(cb.equal(root.get("status"), status));
                    }
                    return cb.and(predicates.toArray(new Predicate[0]));
                };
        return jobRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public SysQuartzJob get(Long id) {
        return jobRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quartz job not found: " + id));
    }

    /** Validates a cron expression. Public so the controller can expose it directly. */
    public boolean validateCron(String cron) {
        return cron != null && CronExpression.isValidExpression(cron);
    }

    @Transactional
    public Long add(SysQuartzJob input) {
        validateInput(input);
        if (jobRepository.existsByJobNameAndJobGroup(input.getJobName(), input.getJobGroup())) {
            throw new IllegalArgumentException(
                    "Quartz job already exists: " + input.getJobName() + "." + input.getJobGroup());
        }
        if (input.getStatus() == null) input.setStatus(SysQuartzJob.STATUS_PAUSED);
        if (input.getMisfirePolicy() == null) input.setMisfirePolicy((short) 1);
        if (input.getConcurrent() == null) input.setConcurrent(false);
        SysQuartzJob saved = jobRepository.save(input);
        try {
            schedule(saved);
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to schedule Quartz job " + saved.getId(), e);
        }
        return saved.getId();
    }

    @Transactional
    public void update(Long id, SysQuartzJob input) {
        SysQuartzJob existing = get(id);
        validateInput(input);
        existing.setJobName(input.getJobName());
        existing.setJobGroup(input.getJobGroup());
        existing.setDescription(input.getDescription());
        existing.setBeanName(input.getBeanName());
        existing.setMethodName(input.getMethodName());
        existing.setMethodParams(input.getMethodParams());
        existing.setCron(input.getCron());
        if (input.getMisfirePolicy() != null) existing.setMisfirePolicy(input.getMisfirePolicy());
        if (input.getConcurrent() != null) existing.setConcurrent(input.getConcurrent());
        SysQuartzJob saved = jobRepository.save(existing);
        try {
            unschedule(saved);
            schedule(saved);
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to reschedule Quartz job " + id, e);
        }
    }

    @Transactional
    public void delete(Long id) {
        SysQuartzJob job = get(id);
        try {
            scheduler.deleteJob(jobKey(job));
        } catch (SchedulerException e) {
            log.warn("Failed to delete Quartz JobDetail for {}", id, e);
        }
        job.setDeleted(true);
        jobRepository.save(job);
    }

    @Transactional
    public void pause(Long id) {
        SysQuartzJob job = get(id);
        try {
            scheduler.pauseJob(jobKey(job));
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to pause Quartz job " + id, e);
        }
        job.pause();
        jobRepository.save(job);
    }

    @Transactional
    public void resume(Long id) {
        SysQuartzJob job = get(id);
        try {
            scheduler.resumeJob(jobKey(job));
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to resume Quartz job " + id, e);
        }
        job.resume();
        jobRepository.save(job);
    }

    /** Triggers an immediate one-shot execution. */
    public void runOnce(Long id) {
        SysQuartzJob job = get(id);
        JobKey key = jobKey(job);
        try {
            if (!scheduler.checkExists(key)) {
                schedule(job);
            }
            scheduler.triggerJob(key, jobDataMap(job));
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to trigger Quartz job " + id, e);
        }
    }

    @Transactional(readOnly = true)
    public Page<SysQuartzLog> logPage(Long jobId, Pageable pageable) {
        return logRepository.findByJobIdOrderByStartedAtDesc(jobId, pageable);
    }

    // ---------- internal scheduler wiring ----------

    private void schedule(SysQuartzJob job) throws SchedulerException {
        JobDetail detail =
                JobBuilder.newJob(QuartzReflectionJob.class)
                        .withIdentity(jobKey(job))
                        .withDescription(job.getDescription())
                        .storeDurably(false)
                        .usingJobData(jobDataMap(job))
                        .build();

        CronScheduleBuilder cronSchedule =
                applyMisfirePolicy(
                        CronScheduleBuilder.cronSchedule(job.getCron()), job.getMisfirePolicy());

        CronTrigger trigger =
                TriggerBuilder.newTrigger()
                        .withIdentity(triggerKey(job))
                        .withSchedule(cronSchedule)
                        .build();

        scheduler.scheduleJob(detail, trigger);

        if (job.isPaused()) {
            scheduler.pauseJob(jobKey(job));
        }
    }

    private void unschedule(SysQuartzJob job) throws SchedulerException {
        scheduler.deleteJob(jobKey(job));
    }

    private JobDataMap jobDataMap(SysQuartzJob job) {
        JobDataMap map = new JobDataMap();
        map.put("jobId", job.getId());
        map.put("jobName", job.getJobName());
        map.put("jobGroup", job.getJobGroup());
        map.put("beanName", job.getBeanName());
        map.put("methodName", job.getMethodName());
        if (job.getMethodParams() != null) {
            map.put("methodParams", job.getMethodParams());
        }
        return map;
    }

    private static JobKey jobKey(SysQuartzJob j) {
        return JobKey.jobKey(j.getJobName(), j.getJobGroup());
    }

    private static TriggerKey triggerKey(SysQuartzJob j) {
        return TriggerKey.triggerKey(j.getJobName(), j.getJobGroup());
    }

    private static CronScheduleBuilder applyMisfirePolicy(
            CronScheduleBuilder builder, Short policy) {
        if (policy == null) return builder;
        return switch (policy.intValue()) {
            case 1 -> builder.withMisfireHandlingInstructionDoNothing();
            case 2 -> builder.withMisfireHandlingInstructionFireAndProceed();
            case 3 -> builder.withMisfireHandlingInstructionIgnoreMisfires();
            default -> builder;
        };
    }

    private void validateInput(SysQuartzJob input) {
        if (input.getJobName() == null || input.getJobName().isBlank()) {
            throw new IllegalArgumentException("jobName is required");
        }
        if (input.getJobGroup() == null || input.getJobGroup().isBlank()) {
            input.setJobGroup("DEFAULT");
        }
        if (input.getBeanName() == null || input.getBeanName().isBlank()) {
            throw new IllegalArgumentException("beanName is required");
        }
        if (input.getMethodName() == null || input.getMethodName().isBlank()) {
            throw new IllegalArgumentException("methodName is required");
        }
        if (!validateCron(input.getCron())) {
            throw new IllegalArgumentException("invalid cron expression: " + input.getCron());
        }
    }
}
