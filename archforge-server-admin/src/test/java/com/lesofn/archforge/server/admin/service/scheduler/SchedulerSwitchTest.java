package com.lesofn.archforge.server.admin.service.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.kagkarlsson.scheduler.Scheduler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/** {@code arch-forge.scheduler.enabled=false} keeps a process out of the db-scheduler cluster (CLI one-shot runs). */
class SchedulerSwitchTest {

    @Test
    void aDisabledSchedulerCreatesNoSchedulerBeans() {
        new ApplicationContextRunner()
                .withUserConfiguration(SchedulerConfig.class, DbSchedulerJobRuntime.class, SchedulerStartupSync.class)
                .withPropertyValues("arch-forge.scheduler.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(Scheduler.class);
                    assertThat(context).doesNotHaveBean(DbSchedulerJobRuntime.class);
                    assertThat(context).doesNotHaveBean(SchedulerStartupSync.class);
                });
    }
}
