package com.lesofn.archforge.server.web.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.server.web.dto.WebDashboardMetricsResponse;
import com.lesofn.archforge.user.api.service.SysLoginLogService;
import com.lesofn.archforge.user.api.service.SysOperLogService;
import com.lesofn.archforge.user.api.service.SysUserService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebDashboardServiceTest {

    @Mock
    private SysUserService userService;

    @Mock
    private SysLoginLogService loginLogService;

    @Mock
    private SysOperLogService operLogService;

    @InjectMocks
    private WebDashboardService service;

    @Test
    void metricsAggregatesModuleCounts() {
        when(userService.countNotDeletedWithStatus(any(Integer.class))).thenReturn(42L);
        when(loginLogService.countSuccessfulSince(any(LocalDateTime.class))).thenReturn(5L);
        when(operLogService.countSince(any(LocalDateTime.class))).thenReturn(9L);

        WebDashboardService spied = spy(service);
        doReturn(7L).when(spied).onlineSessions();

        WebDashboardMetricsResponse metrics = spied.metrics();

        assertEquals(42L, metrics.getUserTotal());
        // used to be the same query as userTotal — every registered user counted as "online"
        assertEquals(7L, metrics.getOnlineNow());
        assertEquals(5L, metrics.getTodayLogin());
        assertEquals(9L, metrics.getTodayOperation());
    }
}
