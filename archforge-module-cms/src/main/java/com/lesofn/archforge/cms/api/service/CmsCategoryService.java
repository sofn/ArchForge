package com.lesofn.archforge.cms.api.service;

import com.lesofn.archforge.cms.api.domain.CmsCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CmsCategoryService {

    List<CmsCategory> listVisible();

    Page<CmsCategory> page(Pageable pageable, String name);

    Optional<CmsCategory> findById(Long id);

    Optional<CmsCategory> findBySlug(String slug);

    CmsCategory create(CmsCategory category);

    CmsCategory update(CmsCategory category);

    void delete(Long id);
}
