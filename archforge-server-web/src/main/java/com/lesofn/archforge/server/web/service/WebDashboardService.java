package com.lesofn.archforge.server.web.service;

import com.lesofn.archforge.common.enums.common.UserStatusEnum;
import com.lesofn.archforge.infrastructure.auth.stp.StpWebUtil;
import com.lesofn.archforge.server.web.dto.WebDashboardMetricsResponse;
import com.lesofn.archforge.user.api.service.SysLoginLogService;
import com.lesofn.archforge.user.api.service.SysOperLogService;
import com.lesofn.archforge.user.api.service.SysUserService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Aggregates the C-end dashboard metrics. */
@Service
@RequiredArgsConstructor
public class WebDashboardService {

    private final SysUserService userService;
    private final SysLoginLogService loginLogService;
    private final SysOperLogService operLogService;

    /**
     * Logged-in web accounts right now = live sa-token account sessions of the web login type.
     * ponytail: one Redis SCAN per call, O(sessions) — fine for a dashboard; cache or keep a gauge if it gets hot.
     */
    protected long onlineSessions() {
        return StpWebUtil.STP_LOGIC.searchSessionId("", 0, -1, false).size();
    }

    public WebDashboardMetricsResponse metrics() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(ZoneId.systemDefault()), LocalTime.MIN);
        long userTotal = userService.countNotDeletedWithStatus(UserStatusEnum.NORMAL.getValue());
        long onlineNow = onlineSessions();
        long todayLogin = loginLogService.countSuccessfulSince(todayStart);
        long todayOperation = operLogService.countSince(todayStart);
        return WebDashboardMetricsResponse.builder()
                .userTotal(userTotal)
                .onlineNow(onlineNow)
                .todayLogin(todayLogin)
                .todayOperation(todayOperation)
                .build();
    }
}
