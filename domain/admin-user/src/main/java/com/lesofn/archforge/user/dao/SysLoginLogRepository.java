package com.lesofn.archforge.user.dao;

import com.lesofn.archforge.user.domain.SysLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SysLoginLogRepository
        extends JpaRepository<SysLoginLog, Long>,
                JpaSpecificationExecutor<SysLoginLog>,
                SysLoginLogRepositoryCustom {}
