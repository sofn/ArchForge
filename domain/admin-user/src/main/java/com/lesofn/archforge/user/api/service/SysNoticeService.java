package com.lesofn.archforge.user.api.service;

import com.lesofn.archforge.user.api.dao.SysNoticeRepository;
import com.lesofn.archforge.user.api.domain.SysNotice;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public interface SysNoticeService {
    Optional<SysNotice> findById(Long noticeId);

    Page<SysNotice> findAll(Pageable pageable);

    SysNotice create(SysNotice notice);

    SysNotice update(SysNotice notice);

    void deleteById(Long noticeId);
}
