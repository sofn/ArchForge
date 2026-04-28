package com.lesofn.archsmith.server.admin.service.quartz;

import com.lesofn.archsmith.common.utils.jackson.JsonUtil;
import com.lesofn.archsmith.user.dao.SysQuartzJobRepository;
import com.lesofn.archsmith.user.dao.SysQuartzLogRepository;
import com.lesofn.archsmith.user.domain.SysQuartzJob;
import com.lesofn.archsmith.user.domain.SysQuartzLog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Single Quartz {@link Job} implementation that dispatches to a Spring bean method by reflection.
 *
 * <p>Configuration is read from the trigger {@link JobDataMap}:
 *
 * <ul>
 *   <li>{@code jobId} – primary key of {@link SysQuartzJob}
 *   <li>{@code jobName}, {@code jobGroup} – Quartz identifiers (also persisted on the log row)
 *   <li>{@code beanName} – Spring bean name to look up
 *   <li>{@code methodName} – method on the bean to invoke
 *   <li>{@code methodParams} – JSON array of arguments (string/number/boolean primitives), or null
 * </ul>
 *
 * <p>Each invocation persists a {@link SysQuartzLog} row capturing duration and any error.
 *
 * @author sofn
 */
@Slf4j
public class QuartzReflectionJob implements Job {

    @Autowired private ApplicationContext applicationContext;
    @Autowired private SysQuartzJobRepository jobRepository;
    @Autowired private SysQuartzLogRepository logRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getMergedJobDataMap();
        Long jobId = data.containsKey("jobId") ? data.getLong("jobId") : null;
        String jobName = data.getString("jobName");
        String jobGroup = data.getString("jobGroup");
        String beanName = data.getString("beanName");
        String methodName = data.getString("methodName");
        String methodParams = data.getString("methodParams");

        // Resolve a SysQuartzJob to attach the log to. Falls back to a transient stub if the row
        // was deleted before the trigger fired.
        SysQuartzJob jobEntity = resolveJob(jobId, jobName, jobGroup, beanName, methodName);

        long startNs = System.currentTimeMillis();
        LocalDateTime startedAt = LocalDateTime.now();
        try {
            Object bean = applicationContext.getBean(beanName);
            Object[] args = parseParams(methodParams);
            Method method = findMethod(bean.getClass(), methodName, args.length);
            method.invoke(bean, args);
            long duration = System.currentTimeMillis() - startNs;
            logRepository.save(
                    SysQuartzLog.success(
                            jobEntity, methodParams, duration, startedAt, LocalDateTime.now()));
        } catch (InvocationTargetException e) {
            long duration = System.currentTimeMillis() - startNs;
            Throwable cause = e.getCause() == null ? e : e.getCause();
            log.error(
                    "[Quartz] reflective job failed bean={} method={} params={}",
                    beanName,
                    methodName,
                    methodParams,
                    cause);
            logRepository.save(
                    SysQuartzLog.failure(
                            jobEntity,
                            methodParams,
                            cause.toString(),
                            duration,
                            startedAt,
                            LocalDateTime.now()));
            throw new JobExecutionException(cause);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startNs;
            log.error(
                    "[Quartz] reflective job dispatch failure bean={} method={} params={}",
                    beanName,
                    methodName,
                    methodParams,
                    e);
            logRepository.save(
                    SysQuartzLog.failure(
                            jobEntity,
                            methodParams,
                            e.toString(),
                            duration,
                            startedAt,
                            LocalDateTime.now()));
            throw new JobExecutionException(e);
        }
    }

    private SysQuartzJob resolveJob(
            Long jobId, String jobName, String jobGroup, String beanName, String methodName) {
        if (jobId != null) {
            SysQuartzJob found = jobRepository.findById(jobId).orElse(null);
            if (found != null) return found;
        }
        return jobRepository
                .findByJobNameAndJobGroup(jobName, jobGroup)
                .orElseGet(
                        () ->
                                new SysQuartzJob()
                                        .setJobName(jobName)
                                        .setJobGroup(jobGroup)
                                        .setBeanName(beanName)
                                        .setMethodName(methodName));
    }

    private static Method findMethod(Class<?> type, String name, int arity)
            throws NoSuchMethodException {
        for (Method m : type.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == arity) {
                return m;
            }
        }
        throw new NoSuchMethodException(type.getName() + "#" + name + " (arity " + arity + ")");
    }

    private static Object[] parseParams(String params) {
        if (params == null || params.isBlank()) {
            return new Object[0];
        }
        // Stored as a JSON array of primitives (string/number/boolean) for transparency.
        List<Object> parsed = JsonUtil.fromList(params, Object.class);
        return parsed == null ? new Object[0] : parsed.toArray();
    }
}
