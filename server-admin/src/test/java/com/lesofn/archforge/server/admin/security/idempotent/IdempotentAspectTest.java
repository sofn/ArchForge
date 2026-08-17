package com.lesofn.archforge.server.admin.security.idempotent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.infrastructure.annotation.Idempotent;
import com.lesofn.archforge.infrastructure.annotation.IdempotentType;
import com.lesofn.archforge.infrastructure.aspect.IdempotentAspect;
import com.lesofn.archforge.infrastructure.auth.LoginContext;
import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.frame.context.RequestContext;
import com.lesofn.archforge.infrastructure.frame.context.ScopedValueContext;
import com.lesofn.archforge.infrastructure.security.SecurityException;
import com.lesofn.archforge.infrastructure.security.idempotent.IdempotentProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IdempotentAspectTest {

    private static final String TOKEN = "test-token-uuid";

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RBucket<String> bucket;

    @Mock
    private ProceedingJoinPoint point;

    @Mock
    private MethodSignature signature;

    private IdempotentProperties properties;
    private IdempotentAspect aspect;

    @BeforeEach
    void setUp() {
        properties = new IdempotentProperties();
        properties.setTokenPrefix("idem:token:");
        properties.setKeyPrefix("idem:lock:");
        properties.setHeaderName("X-Idempotent-Token");
        aspect = new IdempotentAspect(redissonClient, properties);
        when(redissonClient.<String> getBucket(anyString())).thenReturn(bucket);
    }

    @Test
    void tokenModeValidTokenConsumesAndProceeds() throws Throwable {
        Idempotent idempotent = annotation(IdempotentType.TOKEN, 10, "", properties.getHeaderName());
        when(bucket.getAndDelete()).thenReturn("1");
        when(point.proceed()).thenReturn("ok");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(properties.getHeaderName())).thenReturn(TOKEN);

        RequestContext ctx = new RequestContext("test");
        ctx.setOriginRequest(request);

        try (MockedStatic<ScopedValueContext> ctxMock = mockStatic(ScopedValueContext.class)) {
            ctxMock.when(ScopedValueContext::getRequestContext).thenReturn(ctx);
            Object result = aspect.around(point, idempotent);
            assertEquals("ok", result);
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(redissonClient, times(1)).getBucket(keyCaptor.capture());
            verify(bucket, times(1)).getAndDelete();
        }
    }

    @Test
    void tokenModeReusedTokenThrowsInvalid() {
        Idempotent idempotent = annotation(IdempotentType.TOKEN, 10, "", properties.getHeaderName());
        when(bucket.getAndDelete()).thenReturn(null);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(properties.getHeaderName())).thenReturn(TOKEN);
        RequestContext ctx = new RequestContext("test");
        ctx.setOriginRequest(request);
        try (MockedStatic<ScopedValueContext> ctxMock = mockStatic(ScopedValueContext.class)) {
            ctxMock.when(ScopedValueContext::getRequestContext).thenReturn(ctx);
            assertThrows(SecurityException.class, () -> aspect.around(point, idempotent));
        }
    }

    @Test
    void paramModeFirstRequestLocksAndProceeds() throws Throwable {
        Idempotent idempotent = annotation(IdempotentType.PARAM, 5, "", "");
        when(signature.getDeclaringTypeName()).thenReturn("com.lesofn.archforge.TestService");
        when(signature.getName()).thenReturn("save");
        when(point.getSignature()).thenReturn(signature);
        when(point.getArgs()).thenReturn(new Object[] {});
        when(bucket.setIfAbsent(eq("1"), any(Duration.class))).thenReturn(true);
        when(point.proceed()).thenReturn("ok");

        try (MockedStatic<LoginContext> loginMock = mockStatic(LoginContext.class)) {
            loginMock.when(LoginContext::findAdminUser)
                    .thenReturn(Optional.of(new SystemLoginUser(1L, false, "test", "pwd", null, null)));
            Object result = aspect.around(point, idempotent);
            assertEquals("ok", result);
            verify(bucket).setIfAbsent(eq("1"), any(Duration.class));
        }
    }

    @Test
    void paramModeDuplicateRequestThrowsReject() {
        Idempotent idempotent = annotation(IdempotentType.PARAM, 5, "", "");
        when(signature.getDeclaringTypeName()).thenReturn("com.lesofn.archforge.TestService");
        when(signature.getName()).thenReturn("save");
        when(point.getSignature()).thenReturn(signature);
        when(point.getArgs()).thenReturn(new Object[] {});
        when(bucket.setIfAbsent(eq("1"), any(Duration.class))).thenReturn(false);
        try (MockedStatic<LoginContext> loginMock = mockStatic(LoginContext.class)) {
            loginMock.when(LoginContext::findAdminUser)
                    .thenReturn(Optional.of(new SystemLoginUser(1L, false, "test", "pwd", null, null)));
            assertThrows(SecurityException.class, () -> aspect.around(point, idempotent));
        }
    }

    private Idempotent annotation(IdempotentType type, long expireSeconds, String key, String header) {
        Idempotent mock = mock(Idempotent.class);
        when(mock.type()).thenReturn(type);
        when(mock.expireSeconds()).thenReturn(expireSeconds);
        when(mock.key()).thenReturn(key);
        when(mock.header()).thenReturn(header);
        return mock;
    }
}
