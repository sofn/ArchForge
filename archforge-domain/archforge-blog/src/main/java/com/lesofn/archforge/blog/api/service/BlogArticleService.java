package com.lesofn.archforge.blog.api.service;

import com.lesofn.archforge.blog.api.domain.BlogArticle;
import com.lesofn.archforge.blog.api.enums.BlogArticleStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlogArticleService {

    Page<BlogArticle> pagePublished(Pageable pageable, Long categoryId, String keyword);

    Page<BlogArticle> pageAll(Pageable pageable, Long categoryId, String keyword, BlogArticleStatus status);

    Page<BlogArticle> pageByAuthorId(Pageable pageable, Long authorId);

    Optional<BlogArticle> findPublishedBySlug(String slug);

    Optional<BlogArticle> findById(Long id);

    BlogArticle create(BlogArticle article);

    BlogArticle update(BlogArticle article);

    void delete(Long id);

    BlogArticle publish(Long id);

    BlogArticle offline(Long id);
}
