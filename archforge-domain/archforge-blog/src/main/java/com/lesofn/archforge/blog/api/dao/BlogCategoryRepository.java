package com.lesofn.archforge.blog.api.dao;

import com.lesofn.archforge.blog.api.domain.BlogCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogCategoryRepository extends JpaRepository<BlogCategory, Long>, JpaSpecificationExecutor<BlogCategory> {

    Optional<BlogCategory> findBySlugAndDeletedFalse(String slug);

    boolean existsBySlugAndDeletedFalse(String slug);

    List<BlogCategory> findByStatusAndDeletedFalseOrderBySortOrderAsc(Integer status);
}
