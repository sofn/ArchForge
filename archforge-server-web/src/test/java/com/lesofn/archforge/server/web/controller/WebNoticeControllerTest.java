package com.lesofn.archforge.server.web.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lesofn.archforge.server.web.dto.WebNoticeResponse;
import com.lesofn.archforge.server.web.dto.WebOperationLogResponse;
import com.lesofn.archforge.server.web.service.WebNoticeService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class WebNoticeControllerTest {

    @Mock
    private WebNoticeService webNoticeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new WebNoticeController(webNoticeService)).build();
    }

    @Test
    void noticesReturnsServiceList() throws Exception {
        when(webNoticeService.latestNotices()).thenReturn(List.of(WebNoticeResponse.builder()
                .id(1L)
                .title("系统维护")
                .content("今晚升级")
                .noticeType(2)
                .createTime(LocalDateTime.of(2026, 9, 12, 10, 0))
                .build()));

        mockMvc.perform(get("/web/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("系统维护"))
                .andExpect(jsonPath("$[0].noticeType").value(2));
    }

    @Test
    void operationLogsReturnsServiceList() throws Exception {
        when(webNoticeService.latestOperationLogs()).thenReturn(List.of(WebOperationLogResponse.builder()
                .id(9L)
                .username("alice")
                .module("文章")
                .summary("发布文章")
                .operatingTime(LocalDateTime.of(2026, 9, 12, 11, 0))
                .build()));

        mockMvc.perform(get("/web/operation-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9))
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[0].summary").value("发布文章"));
    }
}
