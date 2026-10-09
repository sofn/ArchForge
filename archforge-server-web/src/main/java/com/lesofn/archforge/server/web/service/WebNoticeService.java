package com.lesofn.archforge.server.web.service;

import com.lesofn.archforge.server.web.dto.WebNoticeResponse;
import com.lesofn.archforge.server.web.dto.WebOperationLogResponse;
import com.lesofn.archforge.user.api.service.SysNoticeService;
import com.lesofn.archforge.user.api.service.SysOperLogService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Latest notices and operation logs shown on the C-end dashboard. */
@Service
@RequiredArgsConstructor
public class WebNoticeService {

    private static final int LATEST_LIMIT = 20;

    private final SysNoticeService noticeService;
    private final SysOperLogService operLogService;

    public List<WebNoticeResponse> latestNotices() {
        return noticeService
                .latestPublished(LATEST_LIMIT)
                .stream()
                .map(
                        n -> WebNoticeResponse.builder()
                                .id(n.getNoticeId())
                                .title(n.getNoticeTitle())
                                .content(n.getNoticeContent())
                                .noticeType(n.getNoticeType())
                                .createTime(n.getCreateTime())
                                .build())
                .collect(Collectors.toList());
    }

    public List<WebOperationLogResponse> latestOperationLogs() {
        return operLogService
                .latest(LATEST_LIMIT)
                .stream()
                .map(
                        l -> WebOperationLogResponse.builder()
                                .id(l.getOperId())
                                .username(l.getUsername())
                                .module(l.getModule())
                                .summary(l.getSummary())
                                .operatingTime(l.getOperatingTime())
                                .build())
                .collect(Collectors.toList());
    }
}
