package com.lesofn.archforge.server.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebDashboardMetricsResponse {

    private Long userTotal;
    private Long onlineNow;
    private Long todayLogin;
    private Long todayOperation;
}
