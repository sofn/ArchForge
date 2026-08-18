package com.lesofn.archforge.user.internal.service;

import com.lesofn.archforge.user.api.service.SysLoginLogService;
import com.lesofn.archforge.user.api.dao.SysLoginLogRepository;
import com.lesofn.archforge.user.api.domain.SysLoginLog;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysLoginLogServiceImpl implements SysLoginLogService {

    private final SysLoginLogRepository loginLogRepository;

    public Optional<SysLoginLog> findById(Long infoId) {
        return loginLogRepository.findById(infoId);
    }

    public Page<SysLoginLog> findAll(Pageable pageable) {
        return loginLogRepository.findAll(pageable);
    }

    @Transactional
    public SysLoginLog create(SysLoginLog loginLog) {
        return loginLogRepository.save(loginLog);
    }

    @Transactional
    public void deleteById(Long infoId) {
        loginLogRepository.deleteById(infoId);
    }

    @Transactional
    public void clearAll() {
        loginLogRepository.clearAll();
    }
}
