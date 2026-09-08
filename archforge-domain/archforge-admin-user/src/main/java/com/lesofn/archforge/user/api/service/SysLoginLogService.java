package com.lesofn.archforge.user.api.service;

import com.lesofn.archforge.user.api.domain.SysLoginLog;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SysLoginLogService {
    Optional<SysLoginLog> findById(Long infoId);

    Page<SysLoginLog> findAll(Pageable pageable);

    SysLoginLog create(SysLoginLog loginLog);

    void deleteById(Long infoId);

    void clearAll();
}
