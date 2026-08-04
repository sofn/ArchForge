package com.lesofn.archforge.blog.api.service;

import com.lesofn.archforge.blog.api.domain.BlogCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlogCategoryService {

    List<BlogCategory> listVisible();

    Page<BlogCategory> page(Pageable pageable, String name);

    Optional<BlogCategory> findById(Long id);

    Optional<BlogCategory> findBySlug(String slug);

    BlogCategory create(BlogCategory category);

    BlogCategory update(BlogCategory category);

    void delete(Long id);
}
