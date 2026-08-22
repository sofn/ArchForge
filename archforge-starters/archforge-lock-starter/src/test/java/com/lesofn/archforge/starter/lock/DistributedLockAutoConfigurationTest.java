package com.lesofn.archforge.starter.lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestApplication.class)
@Tag("slow")
public class DistributedLockAutoConfigurationTest {

    private final DistributedLockService distributedLockService;
    private final RedissonClient redissonClient;

    @Autowired
    public DistributedLockAutoConfigurationTest(DistributedLockService distributedLockService,
            RedissonClient redissonClient) {
        this.distributedLockService = distributedLockService;
        this.redissonClient = redissonClient;
    }

    @Test
    public void shouldAcquireAndReleaseLock() {
        String result = this.distributedLockService.executeWithLock("test", () -> "ok");

        assertEquals("ok", result);

        RLock lock = this.redissonClient.getLock("arch:lock:test");
        assertNotNull(lock);
        assertFalse(lock.isLocked());
    }
}
