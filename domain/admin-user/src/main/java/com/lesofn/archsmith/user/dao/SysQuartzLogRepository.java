package com.lesofn.archsmith.user.dao;

import com.lesofn.archsmith.user.domain.SysQuartzLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysQuartzLogRepository extends JpaRepository<SysQuartzLog, Long> {

    Page<SysQuartzLog> findByJobIdOrderByStartedAtDesc(Long jobId, Pageable pageable);
}
