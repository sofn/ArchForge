package com.lesofn.archforge.user.internal.service;

import com.lesofn.archforge.user.api.service.SysLoginLogService;
import com.lesofn.archforge.user.internal.dao.SysLoginLogRepository;
import com.lesofn.archforge.user.api.domain.SysLoginLog;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SysLoginLogServiceImpl implements SysLoginLogService {

    private final SysLoginLogRepository loginLogRepository;

    @Override
    public Optional<SysLoginLog> findById(Long infoId) {
        return loginLogRepository.findById(infoId);
    }

    @Override
    public Page<SysLoginLog> findAll(Pageable pageable) {
        return loginLogRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public SysLoginLog create(SysLoginLog loginLog) {
        return loginLogRepository.save(loginLog);
    }

    @Override
    @Transactional
    public void deleteById(Long infoId) {
        loginLogRepository.deleteById(infoId);
    }

    @Override
    @Transactional
    public void clearAll() {
        loginLogRepository.clearAll();
    }

    @Override
    public long countSuccessfulSince(LocalDateTime since) {
        return loginLogRepository.count((root, query, cb) -> cb.and(
                cb.equal(root.get("status"), 1),
                cb.greaterThanOrEqualTo(root.get("loginTime"), since)));
    }
}
