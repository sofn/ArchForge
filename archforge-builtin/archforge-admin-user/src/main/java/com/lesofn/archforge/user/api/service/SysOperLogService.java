package com.lesofn.archforge.user.api.service;

import com.lesofn.archforge.user.api.domain.SysOperLog;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SysOperLogService {
    Optional<SysOperLog> findById(Long operId);

    Page<SysOperLog> findAll(Pageable pageable);

    SysOperLog create(SysOperLog operLog);

    void deleteById(Long operId);

    void clearAll();
}
