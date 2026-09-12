package com.lesofn.archforge.server.web.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.server.web.dto.WebDashboardMetricsResponse;
import com.lesofn.archforge.user.api.dao.SysLoginLogRepository;
import com.lesofn.archforge.user.api.dao.SysOperLogRepository;
import com.lesofn.archforge.user.api.dao.SysUserRepository;
import com.lesofn.archforge.user.api.domain.SysLoginLog;
import com.lesofn.archforge.user.api.domain.SysOperLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class WebDashboardServiceTest {

    @Mock
    private SysUserRepository userRepository;

    @Mock
    private SysLoginLogRepository sysLoginLogRepository;

    @Mock
    private SysOperLogRepository sysOperLogRepository;

    @InjectMocks
    private WebDashboardService service;

    @Test
    void metricsAggregatesRepositoryCounts() {
        when(userRepository.countByDeletedFalseAndStatus(any(Integer.class))).thenReturn(42L);
        when(sysLoginLogRepository.count(ArgumentMatchers.<Specification<SysLoginLog>> any()))
                .thenReturn(5L);
        when(sysOperLogRepository.count(ArgumentMatchers.<Specification<SysOperLog>> any()))
                .thenReturn(9L);

        WebDashboardMetricsResponse metrics = service.metrics();

        assertEquals(42L, metrics.getUserTotal());
        assertEquals(42L, metrics.getOnlineNow());
        assertEquals(5L, metrics.getTodayLogin());
        assertEquals(9L, metrics.getTodayOperation());
    }
}
