package com.lesofn.archforge.blog.internal.service;

import com.lesofn.archforge.blog.api.dao.BlogArticleRepository;
import com.lesofn.archforge.blog.api.dao.BlogCategoryRepository;
import com.lesofn.archforge.blog.api.domain.BlogArticle;
import com.lesofn.archforge.blog.api.enums.BlogArticleStatus;
import com.lesofn.archforge.blog.api.errors.BlogErrorCode;
import com.lesofn.archforge.blog.api.errors.BlogException;
import com.lesofn.archforge.blog.api.service.BlogArticleService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BlogArticleServiceImpl implements BlogArticleService {

    private final BlogArticleRepository articleRepository;
    private final BlogCategoryRepository categoryRepository;

    private Specification<BlogArticle> buildSpec(Long categoryId, String keyword, BlogArticleStatus status) {
        return (root, query, cb) -> {
            jakarta.persistence.criteria.Predicate predicate = cb.equal(root.get("deleted"), false);
            if (categoryId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("categoryId"), categoryId));
            }
            if (StringUtils.hasText(keyword)) {
                predicate = cb.and(predicate,
                        cb.or(
                                cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
                                cb.like(cb.lower(root.get("summary")), "%" + keyword.toLowerCase() + "%")));
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            return predicate;
        };
    }

    @Override
    public Page<BlogArticle> pagePublished(Pageable pageable, Long categoryId, String keyword) {
        return articleRepository.findAll(buildSpec(categoryId, keyword, BlogArticleStatus.PUBLISHED), pageable);
    }

    @Override
    public Page<BlogArticle> pageAll(Pageable pageable, Long categoryId, String keyword, BlogArticleStatus status) {
        return articleRepository.findAll(buildSpec(categoryId, keyword, status), pageable);
    }

    @Override
    public Page<BlogArticle> pageByAuthorId(Pageable pageable, Long authorId) {
        return articleRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("deleted"), false),
                        cb.equal(root.get("authorId"), authorId)),
                pageable);
    }

    @Override
    public Optional<BlogArticle> findPublishedBySlug(String slug) {
        return articleRepository.findBySlugAndStatusAndDeletedFalse(slug, BlogArticleStatus.PUBLISHED);
    }

    @Override
    public Optional<BlogArticle> findById(Long id) {
        return articleRepository.findById(id);
    }

    @Override
    @Transactional("blogTransactionManager")
    public BlogArticle create(BlogArticle article) {
        validateCategory(article.getCategoryId());
        if (articleRepository.findBySlugAndStatusAndDeletedFalse(article.getSlug(), BlogArticleStatus.DRAFT).isPresent() ||
                articleRepository.findBySlugAndStatusAndDeletedFalse(article.getSlug(), BlogArticleStatus.PUBLISHED)
                        .isPresent() || articleRepository.findBySlugAndStatusAndDeletedFalse(article.getSlug(),
                                BlogArticleStatus.OFFLINE).isPresent()) {
            throw new BlogException(BlogErrorCode.SLUG_EXISTS);
        }
        if (article.getStatus() == BlogArticleStatus.PUBLISHED) {
            article.setPublishTime(java.time.LocalDateTime.now());
        }
        return articleRepository.save(article);
    }

    @Override
    @Transactional("blogTransactionManager")
    public BlogArticle update(BlogArticle article) {
        BlogArticle existing = articleRepository.findById(article.getId())
                .orElseThrow(() -> new BlogException(BlogErrorCode.ARTICLE_NOT_FOUND));
        validateCategory(article.getCategoryId());
        checkSlugUnique(article.getSlug(), article.getId());
        existing.setCategoryId(article.getCategoryId())
                .setTitle(article.getTitle())
                .setSlug(article.getSlug())
                .setSummary(article.getSummary())
                .setContent(article.getContent())
                .setCoverImageFileId(article.getCoverImageFileId())
                .setStatus(article.getStatus());
        if (article.getStatus() == BlogArticleStatus.PUBLISHED && existing.getPublishTime() == null) {
            existing.setPublishTime(java.time.LocalDateTime.now());
        }
        return articleRepository.save(existing);
    }

    @Override
    @Transactional("blogTransactionManager")
    public void delete(Long id) {
        BlogArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new BlogException(BlogErrorCode.ARTICLE_NOT_FOUND));
        articleRepository.delete(article);
    }

    @Override
    @Transactional("blogTransactionManager")
    public BlogArticle publish(Long id) {
        BlogArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new BlogException(BlogErrorCode.ARTICLE_NOT_FOUND));
        return articleRepository.save(article.publish());
    }

    @Override
    @Transactional("blogTransactionManager")
    public BlogArticle offline(Long id) {
        BlogArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new BlogException(BlogErrorCode.ARTICLE_NOT_FOUND));
        return articleRepository.save(article.offline());
    }

    private void validateCategory(Long categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BlogException(BlogErrorCode.CATEGORY_NOT_FOUND));
    }

    private void checkSlugUnique(String slug, Long excludeId) {
        articleRepository.findBySlugAndStatusAndDeletedFalse(slug, BlogArticleStatus.DRAFT)
                .filter(a -> !a.getId().equals(excludeId))
                .ifPresent(a -> {
                    throw new BlogException(BlogErrorCode.SLUG_EXISTS);
                });
        articleRepository.findBySlugAndStatusAndDeletedFalse(slug, BlogArticleStatus.PUBLISHED)
                .filter(a -> !a.getId().equals(excludeId))
                .ifPresent(a -> {
                    throw new BlogException(BlogErrorCode.SLUG_EXISTS);
                });
        articleRepository.findBySlugAndStatusAndDeletedFalse(slug, BlogArticleStatus.OFFLINE)
                .filter(a -> !a.getId().equals(excludeId))
                .ifPresent(a -> {
                    throw new BlogException(BlogErrorCode.SLUG_EXISTS);
                });
    }
}
