package com.lesofn.archforge.server.admin.service.scheduler;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Demo Spring bean target invoked by {@link ReflectionJobHandler}. Demonstrates both no-arg and
 * single-arg method invocation.
 *
 * @author sofn
 */
@Slf4j
@Component("demoSchedulerJob")
public class DemoSchedulerJobBean {

    public void helloWorld() {
        log.info("[DemoSchedulerJob] hello, world @ {}", Instant.now());
    }

    public void printTime(String suffix) {
        log.info("[DemoSchedulerJob] now={} suffix={}", Instant.now(), suffix);
    }

    /** Used by tests to verify reflective invocation succeeds. */
    public String echo(String message) {
        log.info("[DemoSchedulerJob] echo={}", message);
        return message;
    }

    /** Used by tests to verify failure-path logging. */
    public void boom() {
        throw new IllegalStateException("intentional failure");
    }
}
