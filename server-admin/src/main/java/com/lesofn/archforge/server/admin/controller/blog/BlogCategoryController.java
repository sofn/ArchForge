package com.lesofn.archforge.server.admin.controller.blog;

import com.lesofn.archforge.blog.api.enums.BlogArticleStatus;
import com.lesofn.archforge.blog.api.domain.BlogCategory;
import com.lesofn.archforge.blog.api.service.BlogArticleService;
import com.lesofn.archforge.blog.api.service.BlogCategoryService;
import com.lesofn.archforge.server.admin.dto.AdminPageResponse;
import com.lesofn.archforge.server.admin.dto.request.AdminBlogCategoryCreateRequest;
import com.lesofn.archforge.server.admin.dto.request.AdminBlogCategoryListRequest;
import com.lesofn.archforge.server.admin.dto.request.AdminBlogCategoryUpdateRequest;
import com.lesofn.archforge.server.admin.dto.request.IdRequest;
import com.lesofn.archforge.server.admin.dto.response.AdminBlogCategoryResponse;
import com.lesofn.archforge.server.admin.mapper.AdminBlogConvertor;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blog/category")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class BlogCategoryController {

    private final BlogCategoryService categoryService;
    private final BlogArticleService articleService;
    private final AdminBlogConvertor adminBlogMapper;

    @PostMapping
    public AdminPageResponse<AdminBlogCategoryResponse> list(@RequestBody @Valid AdminBlogCategoryListRequest request) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(request.getCurrentPage() - 1, 0),
                request.getPageSize(),
                Sort.by("sortOrder").ascending());
        Page<BlogCategory> page = categoryService.page(pageRequest, request.getName());
        List<AdminBlogCategoryResponse> list = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return AdminPageResponse.of(list, page.getTotalElements(), page.getSize(), request.getCurrentPage());
    }

    @PostMapping("/create")
    public Long create(@RequestBody @Valid AdminBlogCategoryCreateRequest request) {
        BlogCategory category = new BlogCategory()
                .setName(request.getName())
                .setSlug(request.getSlug())
                .setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .setStatus(request.getStatus() == null ? 1 : request.getStatus());
        return categoryService.create(category).getId();
    }

    @PutMapping("/update")
    public Boolean update(@RequestBody @Valid AdminBlogCategoryUpdateRequest request) {
        BlogCategory category = new BlogCategory()
                .setId(request.getId())
                .setName(request.getName())
                .setSlug(request.getSlug())
                .setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .setStatus(request.getStatus() == null ? 1 : request.getStatus());
        categoryService.update(category);
        return true;
    }

    @PostMapping("/delete")
    public Boolean delete(@RequestBody @Valid IdRequest request) {
        categoryService.delete(request.getId());
        return true;
    }

    private AdminBlogCategoryResponse toResponse(BlogCategory category) {
        AdminBlogCategoryResponse response = adminBlogMapper.toCategoryResponse(category);
        long articleCount = articleService.pageAll(
                PageRequest.of(0, 1, Sort.by("createTime").descending()),
                category.getId(), null, null).getTotalElements();
        response.setStatusLabel(category.isVisible() ? "显示" : "隐藏");
        response.setArticleCount(articleCount);
        return response;
    }
}
