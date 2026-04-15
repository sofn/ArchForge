package com.lesofn.archsmith.user.dao;

import com.lesofn.archsmith.user.domain.SysOperLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SysOperLogRepository
        extends JpaRepository<SysOperLog, Long>,
                JpaSpecificationExecutor<SysOperLog>,
                QuerydslPredicateExecutor<SysOperLog> {

    @Modifying
    @Query("DELETE FROM SysOperLog o WHERE o.deleted = false")
    void clearAll();
}
