package com.lesofn.archforge.server.admin.service.quartz;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.common.error.system.SystemException;
import com.lesofn.archforge.user.api.dao.SysQuartzJobRepository;
import com.lesofn.archforge.user.api.dao.SysQuartzLogRepository;
import com.lesofn.archforge.user.api.domain.SysQuartzJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.springframework.context.ApplicationContext;

/** Unit tests for the reflective job target allowlist ({@code arch-forge.quartz.allowed-job-beans}). */
class QuartzJobServiceTest {

    private ApplicationContext applicationContext;
    private QuartzJobService service;

    @BeforeEach
    void setUp() {
        applicationContext = mock(ApplicationContext.class);
        SysQuartzJobRepository jobRepository = mock(SysQuartzJobRepository.class);
        when(jobRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Scheduler scheduler = mock(Scheduler.class);
        service = new QuartzJobService(jobRepository, mock(
                SysQuartzLogRepository.class), scheduler, applicationContext, "demoQuartzJob");
    }

    @Test
    void addRejectsBeanOutsideAllowlist() {
        SysQuartzJob job = validJob();
        job.setBeanName("environment");

        SystemException ex = assertThrows(SystemException.class, () -> service.add(job));
        assertTrue(ex.getMessage().contains("not allowed"));
    }

    @Test
    void addAcceptsAllowlistedBeanWithPublicDeclaredMethod() {
        SysQuartzJob job = validJob();
        when(applicationContext.containsBean("demoQuartzJob")).thenReturn(true);
        when(applicationContext.getBean("demoQuartzJob")).thenReturn(new DemoQuartzJobBean());

        assertDoesNotThrow(() -> service.add(job));
    }

    @Test
    void addRejectsUnknownMethodName() {
        SysQuartzJob job = validJob();
        job.setMethodName("missingMethod");
        when(applicationContext.containsBean("demoQuartzJob")).thenReturn(true);
        when(applicationContext.getBean("demoQuartzJob")).thenReturn(new DemoQuartzJobBean());

        SystemException ex = assertThrows(SystemException.class, () -> service.add(job));
        assertTrue(ex.getMessage().contains("not invocable"));
    }

    @Test
    void addRejectsBeanNotPresentInContext() {
        SysQuartzJob job = validJob();
        when(applicationContext.containsBean("demoQuartzJob")).thenReturn(false);

        SystemException ex = assertThrows(SystemException.class, () -> service.add(job));
        assertTrue(ex.getMessage().contains("does not exist"));
    }

    private SysQuartzJob validJob() {
        return new SysQuartzJob()
                .setJobName("it-allowlist")
                .setJobGroup("DEFAULT")
                .setBeanName("demoQuartzJob")
                .setMethodName("echo")
                .setMethodParams("[\"arg\"]")
                .setCron("0 0 1 1 1 ? 2099");
    }
}
