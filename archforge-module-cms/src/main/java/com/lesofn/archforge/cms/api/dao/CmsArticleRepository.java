package com.lesofn.archforge.cms.api.dao;

import com.lesofn.archforge.cms.api.domain.CmsArticle;
import com.lesofn.archforge.cms.api.enums.CmsArticleStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CmsArticleRepository extends JpaRepository<CmsArticle, Long>, JpaSpecificationExecutor<CmsArticle> {

    Optional<CmsArticle> findBySlugAndStatusAndDeletedFalse(String slug, CmsArticleStatus status);

    long countByCategoryIdAndStatusAndDeletedFalse(Long categoryId, CmsArticleStatus status);

    long countByDeletedFalse();

    Page<CmsArticle> findByStatusAndDeletedFalse(CmsArticleStatus status, Pageable pageable);

    Page<CmsArticle> findByCategoryIdAndStatusAndDeletedFalse(
            Long categoryId, CmsArticleStatus status, Pageable pageable);
}
