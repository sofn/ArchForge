package com.lesofn.archforge.user.internal.service;

import com.lesofn.archforge.user.api.service.SysOperLogService;
import com.lesofn.archforge.user.internal.dao.SysOperLogRepository;
import com.lesofn.archforge.user.api.domain.SysOperLog;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class SysOperLogServiceImpl implements SysOperLogService {

    private final SysOperLogRepository operLogRepository;

    @Override
    public Optional<SysOperLog> findById(Long operId) {
        return operLogRepository.findById(operId);
    }

    @Override
    public Page<SysOperLog> findAll(Pageable pageable) {
        return operLogRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public SysOperLog create(SysOperLog operLog) {
        return operLogRepository.save(operLog);
    }

    @Override
    @Transactional
    public void deleteById(Long operId) {
        operLogRepository.deleteById(operId);
    }

    @Override
    @Transactional
    public void clearAll() {
        operLogRepository.clearAll();
    }

    @Override
    public long countSince(LocalDateTime since) {
        return operLogRepository.count((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("operatingTime"), since));
    }

    @Override
    public List<SysOperLog> latest(int limit) {
        return operLogRepository.findAll(PageRequest.of(0, limit, Sort.by("operatingTime").descending())).getContent();
    }
}
