package com.lesofn.archforge.blog.internal.service;

import com.lesofn.archforge.blog.api.dao.BlogArticleRepository;
import com.lesofn.archforge.blog.api.dao.BlogCategoryRepository;
import com.lesofn.archforge.blog.api.domain.BlogCategory;
import com.lesofn.archforge.blog.api.enums.BlogArticleStatus;
import com.lesofn.archforge.blog.api.errors.BlogErrorCode;
import com.lesofn.archforge.blog.api.errors.BlogException;
import com.lesofn.archforge.blog.api.service.BlogCategoryService;
import java.util.List;
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
public class BlogCategoryServiceImpl implements BlogCategoryService {

    private final BlogCategoryRepository categoryRepository;
    private final BlogArticleRepository articleRepository;

    @Override
    public List<BlogCategory> listVisible() {
        return categoryRepository.findByStatusAndDeletedFalseOrderBySortOrderAsc(1);
    }

    @Override
    public Page<BlogCategory> page(Pageable pageable, String name) {
        Specification<BlogCategory> spec = (root, query, cb) -> {
            if (!StringUtils.hasText(name)) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
        return categoryRepository.findAll(spec, pageable);
    }

    @Override
    public Optional<BlogCategory> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Optional<BlogCategory> findBySlug(String slug) {
        return categoryRepository.findBySlugAndDeletedFalse(slug);
    }

    @Override
    @Transactional("blogTransactionManager")
    public BlogCategory create(BlogCategory category) {
        if (categoryRepository.existsBySlugAndDeletedFalse(category.getSlug())) {
            throw new BlogException(BlogErrorCode.SLUG_EXISTS);
        }
        return categoryRepository.save(category);
    }

    @Override
    @Transactional("blogTransactionManager")
    public BlogCategory update(BlogCategory category) {
        BlogCategory existing = categoryRepository.findById(category.getId())
                .orElseThrow(() -> new BlogException(BlogErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.findBySlugAndDeletedFalse(category.getSlug())
                .filter(c -> !c.getId().equals(category.getId()))
                .ifPresent(c -> {
                    throw new BlogException(BlogErrorCode.SLUG_EXISTS);
                });
        existing.setName(category.getName())
                .setSlug(category.getSlug())
                .setSortOrder(category.getSortOrder())
                .setStatus(category.getStatus());
        return categoryRepository.save(existing);
    }

    @Override
    @Transactional("blogTransactionManager")
    public void delete(Long id) {
        BlogCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new BlogException(BlogErrorCode.CATEGORY_NOT_FOUND));
        long count = articleRepository.countByCategoryIdAndStatusAndDeletedFalse(id, BlogArticleStatus.PUBLISHED) +
                articleRepository.countByCategoryIdAndStatusAndDeletedFalse(id, BlogArticleStatus.DRAFT) + articleRepository
                        .countByCategoryIdAndStatusAndDeletedFalse(id, BlogArticleStatus.OFFLINE);
        if (count > 0) {
            throw new BlogException(BlogErrorCode.CATEGORY_HAS_ARTICLES);
        }
        categoryRepository.delete(category);
    }
}
