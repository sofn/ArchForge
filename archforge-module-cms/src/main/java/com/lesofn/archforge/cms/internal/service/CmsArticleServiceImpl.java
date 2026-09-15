package com.lesofn.archforge.cms.internal.service;

import com.lesofn.archforge.cms.api.dao.CmsArticleRepository;
import com.lesofn.archforge.cms.api.dao.CmsCategoryRepository;
import com.lesofn.archforge.cms.api.domain.CmsArticle;
import com.lesofn.archforge.cms.api.enums.CmsArticleStatus;
import com.lesofn.archforge.cms.api.errors.CmsErrorCode;
import com.lesofn.archforge.cms.api.errors.CmsException;
import com.lesofn.archforge.cms.api.service.CmsArticleService;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CmsArticleServiceImpl implements CmsArticleService {

    private final CmsArticleRepository articleRepository;
    private final CmsCategoryRepository categoryRepository;

    private Specification<CmsArticle> buildSpec(@Nullable Long categoryId, @Nullable String keyword,
            @Nullable CmsArticleStatus status) {
        return (root, query, cb) -> {
            jakarta.persistence.criteria.Predicate predicate = cb.equal(root.get("deleted"), false);
            if (categoryId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("categoryId"), categoryId));
            }
            if (StringUtils.hasText(keyword)) {
                predicate = cb.and(predicate,
                        cb.or(
                                cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase(Locale.ROOT) + "%"),
                                cb.like(cb.lower(root.get("summary")), "%" + keyword.toLowerCase(Locale.ROOT) + "%")));
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            return predicate;
        };
    }

    @Override
    public Page<CmsArticle> pagePublished(Pageable pageable, @Nullable Long categoryId, @Nullable String keyword) {
        return articleRepository.findAll(buildSpec(categoryId, keyword, CmsArticleStatus.PUBLISHED), pageable);
    }

    @Override
    public Page<CmsArticle> pageAll(Pageable pageable, @Nullable Long categoryId, @Nullable String keyword,
            @Nullable CmsArticleStatus status) {
        return articleRepository.findAll(buildSpec(categoryId, keyword, status), pageable);
    }

    @Override
    public Page<CmsArticle> pageByAuthorId(Pageable pageable, Long authorId) {
        return articleRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("deleted"), false),
                        cb.equal(root.get("authorId"), authorId)),
                pageable);
    }

    @Override
    public Optional<CmsArticle> findPublishedBySlug(String slug) {
        return articleRepository.findBySlugAndStatusAndDeletedFalse(slug, CmsArticleStatus.PUBLISHED);
    }

    @Override
    public Optional<CmsArticle> findById(Long id) {
        return articleRepository.findById(id);
    }

    @Override
    @Transactional
    public CmsArticle create(CmsArticle article) {
        validateCategory(article.getCategoryId());
        if (articleRepository.findBySlugAndStatusAndDeletedFalse(article.getSlug(), CmsArticleStatus.DRAFT).isPresent() ||
                articleRepository.findBySlugAndStatusAndDeletedFalse(article.getSlug(), CmsArticleStatus.PUBLISHED)
                        .isPresent() || articleRepository.findBySlugAndStatusAndDeletedFalse(article.getSlug(),
                                CmsArticleStatus.OFFLINE).isPresent()) {
            throw new CmsException(CmsErrorCode.SLUG_EXISTS);
        }
        if (article.getStatus() == CmsArticleStatus.PUBLISHED) {
            article.setPublishTime(java.time.LocalDateTime.now(ZoneId.systemDefault()));
        }
        return articleRepository.save(article);
    }

    @Override
    @Transactional
    public CmsArticle update(CmsArticle article) {
        CmsArticle existing = articleRepository.findById(article.getId())
                .orElseThrow(() -> new CmsException(CmsErrorCode.ARTICLE_NOT_FOUND));
        validateCategory(article.getCategoryId());
        checkSlugUnique(article.getSlug(), article.getId());
        existing.setCategoryId(article.getCategoryId())
                .setTitle(article.getTitle())
                .setSlug(article.getSlug())
                .setSummary(article.getSummary())
                .setContent(article.getContent())
                .setCoverImageFileId(article.getCoverImageFileId())
                .setStatus(article.getStatus());
        if (article.getStatus() == CmsArticleStatus.PUBLISHED && existing.getPublishTime() == null) {
            existing.setPublishTime(java.time.LocalDateTime.now(ZoneId.systemDefault()));
        }
        return articleRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CmsArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new CmsException(CmsErrorCode.ARTICLE_NOT_FOUND));
        articleRepository.delete(article);
    }

    @Override
    @Transactional
    public CmsArticle publish(Long id) {
        CmsArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new CmsException(CmsErrorCode.ARTICLE_NOT_FOUND));
        return articleRepository.save(article.publish());
    }

    @Override
    @Transactional
    public CmsArticle offline(Long id) {
        CmsArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new CmsException(CmsErrorCode.ARTICLE_NOT_FOUND));
        return articleRepository.save(article.offline());
    }

    private void validateCategory(Long categoryId) {
        // existence check — discarding orElseThrow's value would silently disable the
        // validation (found by Error Prone's ReturnValueIgnored)
        if (categoryRepository.findById(categoryId).isEmpty()) {
            throw new CmsException(CmsErrorCode.CATEGORY_NOT_FOUND);
        }
    }

    private void checkSlugUnique(String slug, Long excludeId) {
        articleRepository.findBySlugAndStatusAndDeletedFalse(slug, CmsArticleStatus.DRAFT)
                .filter(a -> !a.getId().equals(excludeId))
                .ifPresent(a -> {
                    throw new CmsException(CmsErrorCode.SLUG_EXISTS);
                });
        articleRepository.findBySlugAndStatusAndDeletedFalse(slug, CmsArticleStatus.PUBLISHED)
                .filter(a -> !a.getId().equals(excludeId))
                .ifPresent(a -> {
                    throw new CmsException(CmsErrorCode.SLUG_EXISTS);
                });
        articleRepository.findBySlugAndStatusAndDeletedFalse(slug, CmsArticleStatus.OFFLINE)
                .filter(a -> !a.getId().equals(excludeId))
                .ifPresent(a -> {
                    throw new CmsException(CmsErrorCode.SLUG_EXISTS);
                });
    }
}
