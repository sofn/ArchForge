package com.lesofn.appforge.user.service;

import com.lesofn.appforge.user.dao.SysNoticeRepository;
import com.lesofn.appforge.user.domain.SysNotice;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysNoticeService {

    private final SysNoticeRepository noticeRepository;

    public Optional<SysNotice> findById(Long noticeId) {
        return noticeRepository.findById(noticeId);
    }

    public Page<SysNotice> findAll(Pageable pageable) {
        return noticeRepository.findAll(pageable);
    }

    @Transactional
    public SysNotice create(SysNotice notice) {
        return noticeRepository.save(notice);
    }

    @Transactional
    public SysNotice update(SysNotice notice) {
        return noticeRepository.save(notice);
    }

    @Transactional
    public void deleteById(Long noticeId) {
        noticeRepository.deleteById(noticeId);
    }
}
