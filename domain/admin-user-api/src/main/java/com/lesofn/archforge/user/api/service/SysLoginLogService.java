package com.lesofn.archforge.user.api.service;

import com.lesofn.archforge.user.api.dao.SysLoginLogRepository;
import com.lesofn.archforge.user.api.domain.SysLoginLog;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public interface SysLoginLogService {
    Optional<SysLoginLog> findById(Long infoId);

    Page<SysLoginLog> findAll(Pageable pageable);

    SysLoginLog create(SysLoginLog loginLog);

    void deleteById(Long infoId);

    void clearAll();
}
