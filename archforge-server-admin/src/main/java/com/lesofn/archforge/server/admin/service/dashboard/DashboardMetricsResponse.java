package com.lesofn.archforge.server.admin.service.dashboard;

public record DashboardMetricsResponse(long userCount, long articleCount, long metaTableCount, long taskCount) {
}
