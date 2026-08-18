package com.lesofn.archforge.server.web.controller;

import com.lesofn.archforge.server.web.dto.WebNoticeResponse;
import com.lesofn.archforge.server.web.dto.WebOperationLogResponse;
import com.lesofn.archforge.user.api.dao.SysNoticeRepository;
import com.lesofn.archforge.user.api.dao.SysOperLogRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/web")
@RequiredArgsConstructor
public class WebNoticeController {

    private final SysNoticeRepository sysNoticeRepository;
    private final SysOperLogRepository sysOperLogRepository;

    @GetMapping("/notices")
    public List<WebNoticeResponse> notices() {
        return sysNoticeRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("status"), 1),
                        cb.equal(root.get("deleted"), false)),
                PageRequest.of(0, 20, Sort.by("createTime").descending()))
                .getContent().stream()
                .map(n -> WebNoticeResponse.builder()
                        .id(n.getNoticeId())
                        .title(n.getNoticeTitle())
                        .content(n.getNoticeContent())
                        .noticeType(n.getNoticeType())
                        .createTime(n.getCreateTime())
                        .build())
                .collect(Collectors.toList());
    }

    @GetMapping("/operation-logs")
    public List<WebOperationLogResponse> operationLogs() {
        return sysOperLogRepository.findAll(
                PageRequest.of(0, 20, Sort.by("operatingTime").descending()))
                .getContent().stream()
                .map(l -> WebOperationLogResponse.builder()
                        .id(l.getOperId())
                        .username(l.getUsername())
                        .module(l.getModule())
                        .summary(l.getSummary())
                        .operatingTime(l.getOperatingTime())
                        .build())
                .collect(Collectors.toList());
    }
}
