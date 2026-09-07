package com.lesofn.archforge.server.admin.service.scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.lesofn.archforge.common.error.system.SystemException;
import com.lesofn.archforge.user.api.dao.SysJobLogRepository;
import com.lesofn.archforge.user.api.dao.SysScheduledJobRepository;
import com.lesofn.archforge.user.api.domain.SysScheduledJob;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

/**
 * Unit tests for the reflective job target allowlist ({@code arch-forge.scheduler.allowed-job-beans})
 * and the db-scheduler wiring on create.
 */
class ScheduledJobServiceTest {

    private ApplicationContext applicationContext;
    private SchedulerClient schedulerClient;
    private SysScheduledJobRepository jobRepository;
    private ScheduledJobService service;

    @BeforeEach
    void setUp() {
        applicationContext = mock(ApplicationContext.class);
        schedulerClient = mock(SchedulerClient.class);
        jobRepository = mock(SysScheduledJobRepository.class);
        when(jobRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(schedulerClient.getScheduledExecution(any())).thenReturn(Optional.empty());
        service = new ScheduledJobService(jobRepository, mock(
                SysJobLogRepository.class), schedulerClient, applicationContext, "demoSchedulerJob");
    }

    @Test
    void addRejectsBeanOutsideAllowlist() {
        SysScheduledJob job = validJob();
        job.setBeanName("environment");

        SystemException ex = assertThrows(SystemException.class, () -> service.add(job));
        assertTrue(ex.getMessage().contains("not allowlisted"));
    }

    @Test
    void addAcceptsRunningJobAndSchedulesIt() {
        SysScheduledJob job = validJob();
        job.setStatus(SysScheduledJob.STATUS_RUNNING);
        when(applicationContext.containsBean("demoSchedulerJob")).thenReturn(true);
        when(applicationContext.getBean("demoSchedulerJob")).thenReturn(new DemoSchedulerJobBean());

        assertDoesNotThrow(() -> service.add(job));
        verify(schedulerClient).scheduleIfNotExists(any(
                com.github.kagkarlsson.scheduler.task.SchedulableInstance.class));
    }

    @Test
    void addDefaultsToPausedAndDoesNotSchedule() {
        SysScheduledJob job = validJob(); // status left null -> defaults to PAUSED
        when(applicationContext.containsBean("demoSchedulerJob")).thenReturn(true);
        when(applicationContext.getBean("demoSchedulerJob")).thenReturn(new DemoSchedulerJobBean());

        assertDoesNotThrow(() -> service.add(job));
        verify(schedulerClient).cancel(any(
                com.github.kagkarlsson.scheduler.task.TaskInstanceId.class));
        verifyNoMoreInteractions(schedulerClient);
    }

    @Test
    void addRejectsUnknownMethodName() {
        SysScheduledJob job = validJob();
        job.setMethodName("missingMethod");
        when(applicationContext.containsBean("demoSchedulerJob")).thenReturn(true);
        when(applicationContext.getBean("demoSchedulerJob")).thenReturn(new DemoSchedulerJobBean());

        SystemException ex = assertThrows(SystemException.class, () -> service.add(job));
        assertTrue(ex.getMessage().contains("not invocable"));
    }

    @Test
    void addRejectsBeanNotPresentInContext() {
        SysScheduledJob job = validJob();
        when(applicationContext.containsBean("demoSchedulerJob")).thenReturn(false);

        SystemException ex = assertThrows(SystemException.class, () -> service.add(job));
        assertTrue(ex.getMessage().contains("does not exist"));
    }

    @Test
    void quartzStyleQuestionMarkIsNormalized() {
        assertEquals("0/30 * * * * *", ScheduledJobService.normalizeCron("0/30 * * * * ?"));
        assertEquals("0 0 1 1 1 *", ScheduledJobService.normalizeCron("0 0 1 1 1 *"));
    }

    @Test
    void validateCronAcceptsSixFieldAndRejectsGarbage() {
        assertTrue(service.validateCron("0/30 * * * * ?"));
        assertTrue(service.validateCron("0 0 1 1 1 *"));
        assertTrue(!service.validateCron("not-a-cron"));
        assertTrue(!service.validateCron(null));
    }

    private SysScheduledJob validJob() {
        return new SysScheduledJob()
                .setJobName("it-allowlist")
                .setJobGroup("DEFAULT")
                .setBeanName("demoSchedulerJob")
                .setMethodName("echo")
                .setMethodParams("[\"arg\"]")
                .setCron("0 0 1 1 1 *");
    }
}
