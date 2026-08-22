package com.lesofn.archforge.server.web.service;

import com.lesofn.archforge.server.web.dto.WebDashboardMetricsResponse;
import com.lesofn.archforge.user.api.dao.SysLoginLogRepository;
import com.lesofn.archforge.user.api.dao.SysOperLogRepository;
import com.lesofn.archforge.user.domain.adapter.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Aggregates the C-end dashboard metrics. */
@Service
@RequiredArgsConstructor
public class WebDashboardService {

    private final UserRepository userRepository;
    private final SysLoginLogRepository sysLoginLogRepository;
    private final SysOperLogRepository sysOperLogRepository;

    public WebDashboardMetricsResponse metrics() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long userTotal = userRepository.countActiveUsers();
        long onlineNow = userRepository.countOnlineUsers();
        long todayLogin = sysLoginLogRepository.count(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("status"), 1),
                        cb.greaterThanOrEqualTo(root.get("loginTime"), todayStart)));
        long todayOperation = sysOperLogRepository.count(
                (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("operatingTime"), todayStart));
        return WebDashboardMetricsResponse.builder()
                .userTotal(userTotal)
                .onlineNow(onlineNow)
                .todayLogin(todayLogin)
                .todayOperation(todayOperation)
                .build();
    }
}
