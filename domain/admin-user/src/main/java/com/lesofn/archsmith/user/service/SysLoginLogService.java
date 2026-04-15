package com.lesofn.archsmith.user.service;

import com.lesofn.archsmith.user.dao.SysLoginLogRepository;
import com.lesofn.archsmith.user.domain.SysLoginLog;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysLoginLogService {

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
