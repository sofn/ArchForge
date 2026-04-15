package com.lesofn.archsmith.user.service;

import com.lesofn.archsmith.user.dao.SysOperLogRepository;
import com.lesofn.archsmith.user.domain.SysOperLog;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysOperLogService {

    private final SysOperLogRepository operLogRepository;

    public Optional<SysOperLog> findById(Long operId) {
        return operLogRepository.findById(operId);
    }

    public Page<SysOperLog> findAll(Pageable pageable) {
        return operLogRepository.findAll(pageable);
    }

    @Transactional
    public SysOperLog create(SysOperLog operLog) {
        return operLogRepository.save(operLog);
    }

    @Transactional
    public void deleteById(Long operId) {
        operLogRepository.deleteById(operId);
    }

    @Transactional
    public void clearAll() {
        operLogRepository.clearAll();
    }
}
