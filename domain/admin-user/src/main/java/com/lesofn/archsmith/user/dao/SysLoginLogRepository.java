package com.lesofn.archsmith.user.dao;

import com.lesofn.archsmith.user.domain.SysLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SysLoginLogRepository
        extends JpaRepository<SysLoginLog, Long>,
                JpaSpecificationExecutor<SysLoginLog>,
                QuerydslPredicateExecutor<SysLoginLog> {

    @Modifying
    @Query("DELETE FROM SysLoginLog l WHERE l.deleted = false")
    void clearAll();
}
