package com.lesofn.appforge.user.dao;

import com.lesofn.appforge.user.domain.SysNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SysNoticeRepository
        extends JpaRepository<SysNotice, Long>,
                JpaSpecificationExecutor<SysNotice>,
                QuerydslPredicateExecutor<SysNotice> {}
