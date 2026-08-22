package com.lesofn.archforge.server.web.controller;

import com.lesofn.archforge.server.web.dto.WebDashboardMetricsResponse;
import com.lesofn.archforge.server.web.service.WebDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/web/dashboard")
@RequiredArgsConstructor
public class WebDashboardController {

    private final WebDashboardService webDashboardService;

    @GetMapping("/metrics")
    public WebDashboardMetricsResponse metrics() {
        return webDashboardService.metrics();
    }
}
