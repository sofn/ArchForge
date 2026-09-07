package com.lesofn.archforge.user.api.dao;

import com.lesofn.archforge.user.api.domain.SysScheduledJob;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SysScheduledJobRepository
        extends JpaRepository<SysScheduledJob, Long>, JpaSpecificationExecutor<SysScheduledJob> {

    Optional<SysScheduledJob> findByJobNameAndJobGroup(String jobName, String jobGroup);

    boolean existsByJobNameAndJobGroup(String jobName, String jobGroup);
}
