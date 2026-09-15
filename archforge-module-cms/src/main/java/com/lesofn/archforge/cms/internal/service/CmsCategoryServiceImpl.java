package com.lesofn.archforge.cms.internal.service;

import com.lesofn.archforge.cms.api.dao.CmsArticleRepository;
import com.lesofn.archforge.cms.api.dao.CmsCategoryRepository;
import com.lesofn.archforge.cms.api.domain.CmsCategory;
import com.lesofn.archforge.cms.api.enums.CmsArticleStatus;
import com.lesofn.archforge.cms.api.errors.CmsErrorCode;
import com.lesofn.archforge.cms.api.errors.CmsException;
import com.lesofn.archforge.cms.api.service.CmsCategoryService;
import java.util.List;
import java.util.Locale;
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
public class CmsCategoryServiceImpl implements CmsCategoryService {

    private final CmsCategoryRepository categoryRepository;
    private final CmsArticleRepository articleRepository;

    @Override
    public List<CmsCategory> listVisible() {
        return categoryRepository.findByStatusAndDeletedFalseOrderBySortOrderAsc(1);
    }

    @Override
    public Page<CmsCategory> page(Pageable pageable, String name) {
        Specification<CmsCategory> spec = (root, query, cb) -> {
            if (!StringUtils.hasText(name)) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase(Locale.ROOT) + "%");
        };
        return categoryRepository.findAll(spec, pageable);
    }

    @Override
    public Optional<CmsCategory> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Optional<CmsCategory> findBySlug(String slug) {
        return categoryRepository.findBySlugAndDeletedFalse(slug);
    }

    @Override
    @Transactional
    public CmsCategory create(CmsCategory category) {
        if (categoryRepository.existsBySlugAndDeletedFalse(category.getSlug())) {
            throw new CmsException(CmsErrorCode.SLUG_EXISTS);
        }
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public CmsCategory update(CmsCategory category) {
        CmsCategory existing = categoryRepository.findById(category.getId())
                .orElseThrow(() -> new CmsException(CmsErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.findBySlugAndDeletedFalse(category.getSlug())
                .filter(c -> !c.getId().equals(category.getId()))
                .ifPresent(c -> {
                    throw new CmsException(CmsErrorCode.SLUG_EXISTS);
                });
        existing.setName(category.getName())
                .setSlug(category.getSlug())
                .setSortOrder(category.getSortOrder())
                .setStatus(category.getStatus());
        return categoryRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CmsCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new CmsException(CmsErrorCode.CATEGORY_NOT_FOUND));
        long count = articleRepository.countByCategoryIdAndStatusAndDeletedFalse(id, CmsArticleStatus.PUBLISHED) +
                articleRepository.countByCategoryIdAndStatusAndDeletedFalse(id, CmsArticleStatus.DRAFT) + articleRepository
                        .countByCategoryIdAndStatusAndDeletedFalse(id, CmsArticleStatus.OFFLINE);
        if (count > 0) {
            throw new CmsException(CmsErrorCode.CATEGORY_HAS_ARTICLES);
        }
        categoryRepository.delete(category);
    }
}
