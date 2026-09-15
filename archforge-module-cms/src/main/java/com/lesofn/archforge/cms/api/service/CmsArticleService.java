package com.lesofn.archforge.cms.api.service;

import com.lesofn.archforge.cms.api.domain.CmsArticle;
import org.jspecify.annotations.Nullable;
import com.lesofn.archforge.cms.api.enums.CmsArticleStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CmsArticleService {

    Page<CmsArticle> pagePublished(Pageable pageable, @Nullable Long categoryId, @Nullable String keyword);

    Page<CmsArticle> pageAll(Pageable pageable, @Nullable Long categoryId, @Nullable String keyword,
            @Nullable CmsArticleStatus status);

    Page<CmsArticle> pageByAuthorId(Pageable pageable, Long authorId);

    Optional<CmsArticle> findPublishedBySlug(String slug);

    Optional<CmsArticle> findById(Long id);

    CmsArticle create(CmsArticle article);

    CmsArticle update(CmsArticle article);

    void delete(Long id);

    CmsArticle publish(Long id);

    CmsArticle offline(Long id);
}
