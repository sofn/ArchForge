package com.lesofn.archforge.infrastructure.aspect;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.common.utils.ip.IpUtil;
import com.lesofn.archforge.infrastructure.annotation.RateLimit;
import com.lesofn.archforge.infrastructure.auth.LoginContext;
import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import com.lesofn.archforge.infrastructure.user.web.RoleInfo;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

/** Tests for rate-limit bucket key resolution across admin and web login contexts. */
class RateLimitAspectTest {

    private StringRedisTemplate redisTemplate;
    private RateLimitAspect aspect;
    private MockedStatic<IpUtil> ipUtil;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenReturn(1L);
        ArchForgeProperties properties = new ArchForgeProperties();
        Environment environment = mock(Environment.class);
        when(environment.matchesProfiles("test")).thenReturn(false);
        aspect = new RateLimitAspect(redisTemplate, properties, environment);
        ipUtil = mockStatic(IpUtil.class);
        ipUtil.when(() -> IpUtil.getClientIp(any(), any())).thenReturn("unknown");
    }

    @AfterEach
    void tearDown() {
        ipUtil.close();
    }

    @Test
    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    void userLimitUsesAdminUserIdWhenAdminLoggedIn() throws Throwable {
        SystemLoginUser admin = new SystemLoginUser(1L, true, "admin", "secret", RoleInfo.EMPTY_ROLE, 4L);
        try (MockedStatic<LoginContext> loginContext = mockStatic(LoginContext.class)) {
            loginContext.when(LoginContext::findAdminUser).thenReturn(Optional.of(admin));

            invokeAndCaptureKey(RateLimit.LimitType.USER);

            verify(redisTemplate)
                    .execute(
                            any(RedisScript.class),
                            argThat(keys -> keys != null && "rate:limit:test-key:user:1".equals(keys.getFirst())),
                            any(Object[].class));
        }
    }

    @Test
    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    void userLimitUsesWebUserIdOnWebApp() throws Throwable {
        try (MockedStatic<LoginContext> loginContext = mockStatic(LoginContext.class)) {
            loginContext.when(LoginContext::findAdminUser).thenReturn(Optional.empty());
            loginContext.when(LoginContext::getWebUserId).thenReturn(99L);

            invokeAndCaptureKey(RateLimit.LimitType.USER);

            verify(redisTemplate)
                    .execute(
                            any(RedisScript.class),
                            argThat(keys -> keys != null && "rate:limit:test-key:user:99".equals(keys.getFirst())),
                            any(Object[].class));
        }
    }

    @Test
    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    void userLimitFallsBackToClientIpBucketForAnonymous() throws Throwable {
        try (MockedStatic<LoginContext> loginContext = mockStatic(LoginContext.class)) {
            loginContext.when(LoginContext::findAdminUser).thenReturn(Optional.empty());
            loginContext.when(LoginContext::getWebUserId).thenReturn(null);

            invokeAndCaptureKey(RateLimit.LimitType.USER);

            verify(redisTemplate)
                    .execute(
                            any(RedisScript.class),
                            argThat(keys -> keys != null && "rate:limit:test-key:user:ip:unknown".equals(keys.getFirst())),
                            any(Object[].class));
        }
    }

    @SuppressWarnings("rawtypes")
    private void invokeAndCaptureKey(RateLimit.LimitType limitType) throws Throwable {
        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.Test");
        when(signature.getName()).thenReturn("action");
        when(point.getSignature()).thenReturn(signature);
        when(point.proceed()).thenReturn(null);
        aspect.around(point, rateLimit(limitType));
    }

    private RateLimit rateLimit(RateLimit.LimitType limitType) {
        return new RateLimit() {
            @Override
            public String key() {
                return "test-key";
            }

            @Override
            public int time() {
                return 60;
            }

            @Override
            public int maxCount() {
                return 100;
            }

            @Override
            public LimitType limitType() {
                return limitType;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return RateLimit.class;
            }
        };
    }
}
