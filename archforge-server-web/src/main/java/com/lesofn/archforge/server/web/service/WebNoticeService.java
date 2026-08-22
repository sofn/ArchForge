package com.lesofn.archforge.server.web.service;

import com.lesofn.archforge.server.web.dto.WebNoticeResponse;
import com.lesofn.archforge.server.web.dto.WebOperationLogResponse;
import com.lesofn.archforge.user.api.dao.SysNoticeRepository;
import com.lesofn.archforge.user.api.dao.SysOperLogRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/** Latest notices and operation logs shown on the C-end dashboard. */
@Service
@RequiredArgsConstructor
public class WebNoticeService {

    private static final int LATEST_LIMIT = 20;

    private final SysNoticeRepository sysNoticeRepository;
    private final SysOperLogRepository sysOperLogRepository;

    public List<WebNoticeResponse> latestNotices() {
        return sysNoticeRepository
                .findAll(
                        (root, query, cb) -> cb.and(
                                cb.equal(root.get("status"), 1),
                                cb.equal(root.get("deleted"), false)),
                        PageRequest.of(0, LATEST_LIMIT, Sort.by("createTime").descending()))
                .getContent()
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
        return sysOperLogRepository
                .findAll(PageRequest.of(0, LATEST_LIMIT, Sort.by("operatingTime").descending()))
                .getContent()
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
