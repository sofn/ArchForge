package com.lesofn.archforge.user.api.dao;

import com.lesofn.archforge.user.api.domain.SysQuartzLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysQuartzLogRepository extends JpaRepository<SysQuartzLog, Long> {

    Page<SysQuartzLog> findByJobIdOrderByStartedAtDesc(Long jobId, Pageable pageable);
}
