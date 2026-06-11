package com.lesofn.archsmith.server.admin.service.quartz;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Demo Spring bean target invoked by {@link QuartzReflectionJob}. Demonstrates both no-arg and
 * single-arg method invocation.
 *
 * @author sofn
 */
@Slf4j
@Component("demoQuartzJob")
public class DemoQuartzJobBean {

    public void helloWorld() {
        log.info("[DemoQuartzJob] hello, world @ {}", Instant.now());
    }

    public void printTime(String suffix) {
        log.info("[DemoQuartzJob] now={} suffix={}", Instant.now(), suffix);
    }

    /** Used by Spock spec to verify reflective invocation succeeds. */
    public String echo(String message) {
        log.info("[DemoQuartzJob] echo={}", message);
        return message;
    }

    /** Used by Spock spec to verify failure-path logging. */
    public void boom() {
        throw new IllegalStateException("intentional failure");
    }
}
