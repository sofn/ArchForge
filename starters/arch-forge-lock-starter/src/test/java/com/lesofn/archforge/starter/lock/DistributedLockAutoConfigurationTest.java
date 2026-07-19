package com.lesofn.archforge.starter.lock;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(RedisTestConfiguration.class)
class DistributedLockAutoConfigurationTest {

    @Autowired
    private DistributedLockService distributedLockService;

    @Autowired
    private LockTestService lockTestService;

    @Test
    void shouldCreateDistributedLockService() {
        assertThat(distributedLockService).isNotNull();
    }

    @Test
    void shouldAcquireAndReleaseLock() {
        RLock lock = distributedLockService.tryLock("manual", 3, 10, TimeUnit.SECONDS);
        assertThat(lock).isNotNull();

        lock.unlock();
        assertThat(lock.isLocked()).isFalse();
    }

    @Test
    void shouldApplyDistributedLockAspect() {
        String result = lockTestService.greet("world");
        assertThat(result).isEqualTo("hello world");
    }
}
