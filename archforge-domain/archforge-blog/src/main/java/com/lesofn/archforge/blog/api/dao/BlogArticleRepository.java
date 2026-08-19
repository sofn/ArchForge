package com.lesofn.archforge.blog.api.dao;

import com.lesofn.archforge.blog.api.domain.BlogArticle;
import com.lesofn.archforge.blog.api.enums.BlogArticleStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogArticleRepository extends JpaRepository<BlogArticle, Long>, JpaSpecificationExecutor<BlogArticle> {

    Optional<BlogArticle> findBySlugAndStatusAndDeletedFalse(String slug, BlogArticleStatus status);

    long countByCategoryIdAndStatusAndDeletedFalse(Long categoryId, BlogArticleStatus status);

    long countByDeletedFalse();

    Page<BlogArticle> findByStatusAndDeletedFalse(BlogArticleStatus status, Pageable pageable);

    Page<BlogArticle> findByCategoryIdAndStatusAndDeletedFalse(
            Long categoryId, BlogArticleStatus status, Pageable pageable);
}
