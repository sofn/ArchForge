package com.lesofn.archforge.server.web.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lesofn.archforge.server.web.dto.WebDashboardMetricsResponse;
import com.lesofn.archforge.server.web.service.WebDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class WebDashboardControllerTest {

    @Mock
    private WebDashboardService webDashboardService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new WebDashboardController(webDashboardService)).build();
    }

    @Test
    void metricsReturnsServicePayload() throws Exception {
        when(webDashboardService.metrics()).thenReturn(WebDashboardMetricsResponse.builder()
                .userTotal(42L)
                .onlineNow(7L)
                .todayLogin(5L)
                .todayOperation(9L)
                .build());

        mockMvc.perform(get("/web/dashboard/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userTotal").value(42))
                .andExpect(jsonPath("$.onlineNow").value(7))
                .andExpect(jsonPath("$.todayLogin").value(5))
                .andExpect(jsonPath("$.todayOperation").value(9));
    }
}
