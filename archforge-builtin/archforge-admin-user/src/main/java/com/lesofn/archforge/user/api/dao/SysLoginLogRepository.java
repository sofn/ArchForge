package com.lesofn.archforge.user.api.dao;

import com.lesofn.archforge.user.api.domain.SysLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SysLoginLogRepository extends JpaRepository<SysLoginLog, Long>, JpaSpecificationExecutor<SysLoginLog>, SysLoginLogRepositoryCustom {
}
