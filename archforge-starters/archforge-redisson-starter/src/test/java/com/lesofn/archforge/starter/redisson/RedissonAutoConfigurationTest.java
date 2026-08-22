package com.lesofn.archforge.starter.redisson;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test for {@link RedissonAutoConfiguration}.
 */
@SpringBootTest(classes = TestApplication.class)
@Tag("slow")
public class RedissonAutoConfigurationTest {

    private final RedissonClient redissonClient;

    @Autowired
    public RedissonAutoConfigurationTest(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Test
    public void testRedissonClientConnection() {
        assertNotNull(redissonClient);

        RBucket<String> bucket = redissonClient.getBucket("test:key");
        String expected = "test-value";
        bucket.set(expected);

        String actual = bucket.get();
        assertEquals(expected, actual);

        bucket.delete();
    }
}
