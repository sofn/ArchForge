package com.lesofn.archforge.user.internal.dao;

import com.lesofn.archforge.user.api.domain.SysOperLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SysOperLogRepository extends JpaRepository<SysOperLog, Long>, JpaSpecificationExecutor<SysOperLog>, SysOperLogRepositoryCustom {
}
