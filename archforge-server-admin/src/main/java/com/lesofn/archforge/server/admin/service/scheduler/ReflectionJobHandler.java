package com.lesofn.archforge.server.admin.service.scheduler;

import com.lesofn.archforge.user.api.dao.SysJobLogRepository;
import com.lesofn.archforge.user.api.domain.SysJobLog;
import com.lesofn.archforge.user.api.domain.SysScheduledJob;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Executes a {@link JobInvocationData} by dispatching to a Spring bean method via reflection,
 * persisting a per-run audit log — the same contract the former QuartzReflectionJob had.
 *
 * <p>
 * Runs on db-scheduler worker threads; the allowlist check happens at admin write time (see
 * ScheduledJobService), keeping this path lean.
 *
 * @author sofn
 */
@Slf4j
@Component
public class ReflectionJobHandler {

    private final SysJobLogRepository logRepository;
    private final ApplicationContext applicationContext;

    public ReflectionJobHandler(SysJobLogRepository logRepository, ApplicationContext applicationContext) {
        this.logRepository = logRepository;
        this.applicationContext = applicationContext;
    }

    public void run(JobInvocationData data) {
        final String beanName = data.beanName();
        final String methodName = data.methodName();
        final String methodParams = data.methodParams();

        // Job row may have been deleted since scheduling; log against a detached snapshot.
        SysScheduledJob snapshot = new SysScheduledJob()
                .setId(data.jobId())
                .setJobName(data.jobName())
                .setJobGroup(data.jobGroup())
                .setBeanName(beanName)
                .setMethodName(methodName);

        long startMs = System.currentTimeMillis();
        LocalDateTime startedAt = LocalDateTime.now();
        try {
            Object bean = applicationContext.getBean(beanName);
            Object[] args = parseParams(methodParams);
            Method method = findMethod(bean.getClass(), methodName, args.length);
            method.invoke(bean, args);
            long duration = System.currentTimeMillis() - startMs;
            logRepository.save(
                    SysJobLog.success(snapshot, methodParams, duration, startedAt, LocalDateTime.now()));
        } catch (InvocationTargetException e) {
            long duration = System.currentTimeMillis() - startMs;
            Throwable cause = e.getCause() == null ? e : e.getCause();
            log.error(
                    "[db-scheduler] reflective job failed bean={} method={} params={}",
                    beanName,
                    methodName,
                    methodParams,
                    cause);
            logRepository.save(
                    SysJobLog.failure(
                            snapshot,
                            methodParams,
                            String.valueOf(cause.getMessage()),
                            duration,
                            startedAt,
                            LocalDateTime.now()));
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startMs;
            log.error(
                    "[db-scheduler] job dispatch failed bean={} method={} params={}",
                    beanName,
                    methodName,
                    methodParams,
                    e);
            logRepository.save(
                    SysJobLog.failure(
                            snapshot,
                            methodParams,
                            String.valueOf(e.getMessage()),
                            duration,
                            startedAt,
                            LocalDateTime.now()));
        }
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
        List<Object> parsed = com.lesofn.archforge.common.utils.jackson.JsonUtil.fromList(params, Object.class);
        return parsed == null ? new Object[0] : parsed.toArray();
    }
}
