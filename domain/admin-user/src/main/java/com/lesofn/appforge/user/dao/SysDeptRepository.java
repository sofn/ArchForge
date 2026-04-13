package com.lesofn.appforge.user.dao;

import com.lesofn.appforge.user.domain.SysDept;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * 部门数据访问层
 *
 * @author sofn
 */
@Repository
public interface SysDeptRepository
        extends JpaRepository<SysDept, Long>,
                JpaSpecificationExecutor<SysDept>,
                QuerydslPredicateExecutor<SysDept> {

    List<SysDept> findByParentId(Long parentId);

    @Query("SELECT d FROM SysDept d WHERE d.deleted = false ORDER BY d.sort ASC")
    List<SysDept> findAllActiveDepts();
}
