package com.lesofn.archforge.user.internal.service;

import com.lesofn.archforge.user.internal.dao.SysJobLogRepository;
import com.lesofn.archforge.user.api.domain.SysJobLog;
import com.lesofn.archforge.user.api.service.SysJobLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysJobLogServiceImpl implements SysJobLogService {

    private final SysJobLogRepository logRepository;

    @Override
    @Transactional
    public SysJobLog record(SysJobLog log) {
        return logRepository.save(log);
    }
}
