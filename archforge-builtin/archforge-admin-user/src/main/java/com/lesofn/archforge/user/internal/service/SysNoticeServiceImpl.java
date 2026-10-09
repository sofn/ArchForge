package com.lesofn.archforge.user.internal.service;

import com.lesofn.archforge.user.api.service.SysNoticeService;
import com.lesofn.archforge.user.internal.dao.SysNoticeRepository;
import com.lesofn.archforge.user.api.domain.SysNotice;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class SysNoticeServiceImpl implements SysNoticeService {

    private final SysNoticeRepository noticeRepository;

    @Override
    public Optional<SysNotice> findById(Long noticeId) {
        return noticeRepository.findById(noticeId);
    }

    @Override
    public Page<SysNotice> findAll(Pageable pageable) {
        return noticeRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public SysNotice create(SysNotice notice) {
        return noticeRepository.save(notice);
    }

    @Override
    @Transactional
    public SysNotice update(SysNotice notice) {
        return noticeRepository.save(notice);
    }

    @Override
    @Transactional
    public void deleteById(Long noticeId) {
        noticeRepository.deleteById(noticeId);
    }

    @Override
    public List<SysNotice> latestPublished(int limit) {
        return noticeRepository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("status"), 1),
                cb.equal(root.get("deleted"), false)),
                PageRequest.of(0, limit, Sort.by("createTime").descending())).getContent();
    }
}
