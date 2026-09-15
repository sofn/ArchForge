package com.lesofn.archforge.user.api.dao;

import com.lesofn.archforge.user.api.domain.SysJobLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysJobLogRepository extends JpaRepository<SysJobLog, Long> {

    Page<SysJobLog> findByJobIdOrderByStartedAtDesc(Long jobId, Pageable pageable);
}
