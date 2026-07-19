package com.lesofn.archforge.starter.redisson;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(RedisTestConfiguration.class)
class RedissonAutoConfigurationTest {

    @Autowired
    private RedissonClient redissonClient;

    @Test
    void shouldCreateRedissonClientAndReadWrite() {
        assertThat(redissonClient).isNotNull();

        RBucket<String> bucket = redissonClient.getBucket("archforge:test:redisson");
        bucket.set("ok");

        assertThat(bucket.get()).isEqualTo("ok");

        bucket.delete();
    }
}
