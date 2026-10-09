package com.lesofn.archforge.user.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.common.error.system.SystemException;
import com.lesofn.archforge.user.api.domain.SysScheduledJob;
import com.lesofn.archforge.user.api.scheduler.SchedulerJobRuntime;
import com.lesofn.archforge.user.internal.dao.SysJobLogRepository;
import com.lesofn.archforge.user.internal.dao.SysScheduledJobRepository;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;

/** Jobs invoke bean methods reflectively: only allow-listed beans with a public method of the right arity. */
class SysScheduledJobServiceImplTest {

    /** A bean the job may call. */
    public static class ReportJob {
        public void run() {
        }

        public void runWith(String argument) {
        }
    }

    private final SysScheduledJobRepository jobRepository = mock(SysScheduledJobRepository.class);
    private final SysJobLogRepository logRepository = mock(SysJobLogRepository.class);
    private final ApplicationContext context = mock(ApplicationContext.class);
    private final SchedulerJobRuntime runtime = mock(SchedulerJobRuntime.class);
    private SysScheduledJobServiceImpl service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        ObjectProvider<SchedulerJobRuntime> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable(any())).thenReturn(runtime);
        when(runtime.isValidCron(any())).thenReturn(true);
        when(context.containsBean("reportJob")).thenReturn(true);
        when(context.containsBean("dataSource")).thenReturn(true);
        when(context.getBean("reportJob")).thenReturn(new ReportJob());
        when(jobRepository.save(any(SysScheduledJob.class))).thenAnswer(invocation -> {
            SysScheduledJob job = invocation.getArgument(0);
            if (job.getId() == null) {
                job.setId(100L);
            }
            return job;
        });
        service = new SysScheduledJobServiceImpl(jobRepository, logRepository, provider, context, "reportJob");
    }

    @Test
    void addStoresAPausedJobAndSyncsTheSchedule() {
        SysScheduledJob job = job("reportJob", "run", null);

        assertEquals(100L, service.add(job));

        assertEquals(SysScheduledJob.STATUS_PAUSED, job.getStatus());
        assertFalse(job.getConcurrent());
        verify(runtime).syncSchedule(job);
    }

    @Test
    void beansOutsideTheAllowListAreRefused() {
        SystemException e = assertThrows(SystemException.class, () -> service.add(job("dataSource", "getConnection", null)));

        assertTrue(String.valueOf(e.getMessage()).contains("not allowlisted"), e.getMessage());
        verify(jobRepository, never()).save(any());
    }

    @Test
    void theMethodMustExistWithTheGivenArity() {
        assertThrows(SystemException.class, () -> service.add(job("reportJob", "nope", null)));
        assertThrows(SystemException.class, () -> service.add(job("reportJob", "run", "[\"extra\"]")));
        SysScheduledJob withArgument = job("reportJob", "runWith", "[\"x\"]");
        withArgument.setJobName("other");

        assertEquals(100L, service.add(withArgument));
    }

    @Test
    void duplicatesAreRefused() {
        when(jobRepository.existsByJobNameAndJobGroup("nightly", "DEFAULT")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.add(job("reportJob", "run", null)));
    }

    @Test
    void lifecycleGoesThroughTheRuntime() {
        SysScheduledJob stored = job("reportJob", "run", null);
        stored.setId(7L);
        when(jobRepository.findById(7L)).thenReturn(Optional.of(stored));

        service.pause(7L);
        verify(runtime).cancelSchedule(stored);
        service.resume(7L);
        verify(runtime).persistSchedule(stored);
        service.runOnce(7L);
        verify(runtime).triggerOnce(stored);
        service.delete(7L);

        assertTrue(stored.getDeleted());
        assertFalse(service.existsActive(7L));
        assertThrows(IllegalArgumentException.class, () -> service.get(8L));
    }

    @Test
    void cronValidationAndListing() {
        when(jobRepository.findAll()).thenReturn(List.of(job("reportJob", "run", null)));

        assertTrue(service.validateCron("0 0 * * * ?"));
        assertFalse(service.validateCron(" "));
        assertEquals(1, service.listAll().size());
    }

    private static SysScheduledJob job(String bean, String method, @Nullable String params) {
        SysScheduledJob job = new SysScheduledJob();
        job.setJobName("nightly");
        job.setJobGroup("DEFAULT");
        job.setBeanName(bean);
        job.setMethodName(method);
        job.setMethodParams(params);
        job.setCron("0 0 * * * ?");
        job.setDeleted(false);
        return job;
    }
}
