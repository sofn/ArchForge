package com.lesofn.archforge.server.web.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.server.web.dto.WebNoticeResponse;
import com.lesofn.archforge.server.web.dto.WebOperationLogResponse;
import com.lesofn.archforge.user.api.dao.SysNoticeRepository;
import com.lesofn.archforge.user.api.dao.SysOperLogRepository;
import com.lesofn.archforge.user.api.domain.SysNotice;
import com.lesofn.archforge.user.api.domain.SysOperLog;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class WebNoticeServiceTest {

    @Mock
    private SysNoticeRepository sysNoticeRepository;

    @Mock
    private SysOperLogRepository sysOperLogRepository;

    @InjectMocks
    private WebNoticeService service;

    @Test
    void latestNoticesMapsEntityFields() {
        SysNotice notice = new SysNotice();
        notice.setNoticeId(1L);
        notice.setNoticeTitle("系统维护");
        notice.setNoticeContent("今晚升级");
        notice.setNoticeType(2);
        notice.setCreateTime(LocalDateTime.of(2026, 9, 12, 10, 0));
        when(sysNoticeRepository.findAll(
                ArgumentMatchers.<Specification<SysNotice>> any(), any(Pageable.class)))
                        .thenReturn(new PageImpl<>(List.of(notice)));

        List<WebNoticeResponse> notices = service.latestNotices();

        assertEquals(1, notices.size());
        assertEquals(1L, notices.get(0).getId());
        assertEquals("系统维护", notices.get(0).getTitle());
        assertEquals("今晚升级", notices.get(0).getContent());
        assertEquals(2, notices.get(0).getNoticeType());
    }

    @Test
    void latestOperationLogsMapsEntityFields() {
        SysOperLog log = new SysOperLog();
        log.setOperId(9L);
        log.setUsername("alice");
        log.setModule("文章");
        log.setSummary("发布文章");
        log.setOperatingTime(LocalDateTime.of(2026, 9, 12, 11, 0));
        when(sysOperLogRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(log)));

        List<WebOperationLogResponse> logs = service.latestOperationLogs();

        assertEquals(1, logs.size());
        assertEquals(9L, logs.get(0).getId());
        assertEquals("alice", logs.get(0).getUsername());
        assertEquals("发布文章", logs.get(0).getSummary());
    }
}
