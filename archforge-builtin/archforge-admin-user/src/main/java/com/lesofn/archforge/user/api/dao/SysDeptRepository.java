package com.lesofn.archforge.user.api.dao;

import com.lesofn.archforge.user.api.domain.SysDept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 部门数据访问层
 *
 * @author sofn
 */
@Repository
public interface SysDeptRepository extends JpaRepository<SysDept, Long>, JpaSpecificationExecutor<SysDept>, SysDeptRepositoryCustom {

    List<SysDept> findByParentId(Long parentId);
}
