package com.lesofn.archforge.server.web.controller;

import com.lesofn.archforge.server.web.dto.WebNoticeResponse;
import com.lesofn.archforge.server.web.dto.WebOperationLogResponse;
import com.lesofn.archforge.server.web.service.WebNoticeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/web")
@RequiredArgsConstructor
public class WebNoticeController {

    private final WebNoticeService webNoticeService;

    @GetMapping("/notices")
    public List<WebNoticeResponse> notices() {
        return webNoticeService.latestNotices();
    }

    @GetMapping("/operation-logs")
    public List<WebOperationLogResponse> operationLogs() {
        return webNoticeService.latestOperationLogs();
    }
}
