package com.lesofn.archsmith.user.dao;

import com.lesofn.archsmith.user.domain.SysQuartzJob;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SysQuartzJobRepository
        extends JpaRepository<SysQuartzJob, Long>, JpaSpecificationExecutor<SysQuartzJob> {

    Optional<SysQuartzJob> findByJobNameAndJobGroup(String jobName, String jobGroup);

    boolean existsByJobNameAndJobGroup(String jobName, String jobGroup);
}
