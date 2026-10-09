package com.lesofn.archforge.user.api.service;

import com.lesofn.archforge.user.api.domain.SysNotice;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface SysNoticeService {
    Optional<SysNotice> findById(Long noticeId);

    Page<SysNotice> findAll(Pageable pageable);

    SysNotice create(SysNotice notice);

    SysNotice update(SysNotice notice);

    void deleteById(Long noticeId);

    /** The {@code limit} most recent published (status 1, not deleted) notices, newest first. */
    List<SysNotice> latestPublished(int limit);
}
