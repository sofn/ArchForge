package com.lesofn.archsmith.user.dao;

import com.lesofn.archsmith.user.domain.SysConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SysConfigRepository
        extends JpaRepository<SysConfig, Long>,
                JpaSpecificationExecutor<SysConfig>,
                QuerydslPredicateExecutor<SysConfig> {

    Optional<SysConfig> findByConfigKey(String configKey);
}
