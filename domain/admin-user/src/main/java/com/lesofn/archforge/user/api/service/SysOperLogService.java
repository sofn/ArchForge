package com.lesofn.archforge.user.api.service;

import com.lesofn.archforge.user.api.dao.SysOperLogRepository;
import com.lesofn.archforge.user.api.domain.SysOperLog;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public interface SysOperLogService {
    Optional<SysOperLog> findById(Long operId);

    Page<SysOperLog> findAll(Pageable pageable);

    SysOperLog create(SysOperLog operLog);

    void deleteById(Long operId);

    void clearAll();
}
