package com.lesofn.archforge.infrastructure.security.idempotent;

import com.lesofn.archforge.infrastructure.frame.response.model.ResponseResult;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 幂等 Token 申请接口。
 *
 * @author sofn
 */
@RestController
@RequestMapping("/idempotent")
@ConditionalOnProperty(prefix = "arch-forge.idempotent", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class IdempotentTokenController {

    private final RedissonClient redissonClient;
    private final IdempotentProperties idempotentProperties;

    /**
     * 申请一次性幂等 Token。
     *
     * <p>
     * 客户端在提交表单前调用本接口获取 Token，并在后续请求中通过 {@code X-Idempotent-Token}
     * 头部携带该 Token；服务端消费一次后失效。
     */
    @GetMapping("/token")
    public ResponseResult<String> token() {
        String token = UUID.randomUUID().toString();
        RBucket<String> bucket = redissonClient.getBucket(idempotentProperties.getTokenPrefix() + token);
        bucket.set("1", Duration.ofSeconds(idempotentProperties.getTokenExpireSeconds()));
        return ResponseResult.success(token);
    }
}
