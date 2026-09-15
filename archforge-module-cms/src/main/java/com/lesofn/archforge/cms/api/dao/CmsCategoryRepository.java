package com.lesofn.archforge.cms.api.dao;

import com.lesofn.archforge.cms.api.domain.CmsCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CmsCategoryRepository extends JpaRepository<CmsCategory, Long>, JpaSpecificationExecutor<CmsCategory> {

    Optional<CmsCategory> findBySlugAndDeletedFalse(String slug);

    boolean existsBySlugAndDeletedFalse(String slug);

    List<CmsCategory> findByStatusAndDeletedFalseOrderBySortOrderAsc(Integer status);
}
