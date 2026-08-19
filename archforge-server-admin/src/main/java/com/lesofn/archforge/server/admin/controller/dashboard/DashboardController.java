package com.lesofn.archforge.server.admin.controller.dashboard;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import com.lesofn.archforge.server.admin.service.dashboard.DashboardActivity;
import com.lesofn.archforge.server.admin.service.dashboard.DashboardMetricsResponse;
import com.lesofn.archforge.server.admin.service.dashboard.DashboardService;
import com.lesofn.archforge.server.admin.service.dashboard.DashboardTodo;
import com.lesofn.archforge.server.admin.service.dashboard.DashboardTrendPoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "仪表盘")
@SaCheckLogin(type = StpAdminUtil.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "仪表盘指标")
    @GetMapping("/metrics")
    public DashboardMetricsResponse metrics() {
        return dashboardService.metrics();
    }

    @Operation(summary = "仪表盘趋势")
    @GetMapping("/trends")
    public List<DashboardTrendPoint> trends(@RequestParam(name = "days", defaultValue = "7") int days) {
        return dashboardService.trends(days);
    }

    @Operation(summary = "最近动态")
    @GetMapping("/recent-activities")
    public List<DashboardActivity> recentActivities() {
        return dashboardService.recentActivities();
    }

    @Operation(summary = "待办聚合")
    @GetMapping("/todo")
    public List<DashboardTodo> todo() {
        return dashboardService.todo();
    }
}
