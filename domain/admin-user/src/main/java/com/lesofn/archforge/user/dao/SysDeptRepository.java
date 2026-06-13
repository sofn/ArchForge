package com.lesofn.archforge.user.dao;

import com.lesofn.archforge.user.domain.SysDept;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
                SysDeptRepositoryCustom {

    List<SysDept> findByParentId(Long parentId);
}
