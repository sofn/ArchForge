package com.lesofn.archforge.server.admin.controller.admin;

import com.lesofn.archforge.blog.api.domain.BlogArticle;
import com.lesofn.archforge.blog.api.domain.BlogCategory;
import com.lesofn.archforge.blog.api.enums.BlogArticleStatus;
import com.lesofn.archforge.blog.api.service.BlogArticleService;
import com.lesofn.archforge.blog.api.service.BlogCategoryService;
import com.lesofn.archforge.infrastructure.auth.AuthenticationUtils;
import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.server.admin.dto.AdminPageResult;
import com.lesofn.archforge.server.admin.dto.request.AdminBlogArticleCreateRequest;
import com.lesofn.archforge.server.admin.dto.request.AdminBlogArticleListRequest;
import com.lesofn.archforge.server.admin.dto.request.AdminBlogArticleUpdateRequest;
import com.lesofn.archforge.server.admin.dto.request.IdRequest;
import com.lesofn.archforge.server.admin.dto.response.AdminBlogArticleResponse;
import com.lesofn.archforge.server.admin.mapper.AdminBlogMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blog/article")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminBlogArticleController {

    private final BlogArticleService articleService;
    private final BlogCategoryService categoryService;
    private final AdminBlogMapper adminBlogMapper;

    @Value("${arch-forge.web.public-url:http://localhost:8081}")
    private String webPublicUrl;

    @PostMapping
    public AdminPageResult<AdminBlogArticleResponse> list(@RequestBody @Valid AdminBlogArticleListRequest request) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(request.getCurrentPage() - 1, 0),
                request.getPageSize(),
                Sort.by("createTime").descending());
        BlogArticleStatus status = toStatus(request.getStatus());
        Page<BlogArticle> page = articleService.pageAll(
                pageRequest, request.getCategoryId(), request.getTitle(), status);
        List<AdminBlogArticleResponse> list = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return AdminPageResult.of(list, page.getTotalElements(), page.getSize(), request.getCurrentPage());
    }

    @PostMapping("/create")
    public Long create(@RequestBody @Valid AdminBlogArticleCreateRequest request) {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        BlogArticle article = buildFromRequest(request)
                .setAuthorId(loginUser != null ? loginUser.getUserId() : null);
        return articleService.create(article).getId();
    }

    @PutMapping("/update")
    public Boolean update(@RequestBody @Valid AdminBlogArticleUpdateRequest request) {
        BlogArticle article = buildFromRequest(request).setId(request.getId());
        articleService.update(article);
        return true;
    }

    @PostMapping("/delete")
    public Boolean delete(@RequestBody @Valid IdRequest request) {
        articleService.delete(request.getId());
        return true;
    }

    @PostMapping("/{id}/publish")
    public Boolean publish(@PathVariable Long id) {
        articleService.publish(id);
        return true;
    }

    @PostMapping("/{id}/offline")
    public Boolean offline(@PathVariable Long id) {
        articleService.offline(id);
        return true;
    }

    private BlogArticleStatus toStatus(Integer value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case 0 -> BlogArticleStatus.DRAFT;
            case 1 -> BlogArticleStatus.PUBLISHED;
            case 2 -> BlogArticleStatus.OFFLINE;
            default -> throw new IllegalArgumentException("状态值无效: " + value);
        };
    }

    private BlogArticle buildFromRequest(AdminBlogArticleCreateRequest request) {
        return new BlogArticle()
                .setCategoryId(request.getCategoryId())
                .setTitle(request.getTitle())
                .setSlug(request.getSlug())
                .setSummary(request.getSummary())
                .setContent(request.getContent())
                .setCoverImageFileId(request.getCoverImageFileId())
                .setStatus(toStatus(request.getStatus()));
    }

    private BlogArticle buildFromRequest(AdminBlogArticleUpdateRequest request) {
        return new BlogArticle()
                .setCategoryId(request.getCategoryId())
                .setTitle(request.getTitle())
                .setSlug(request.getSlug())
                .setSummary(request.getSummary())
                .setContent(request.getContent())
                .setCoverImageFileId(request.getCoverImageFileId())
                .setStatus(toStatus(request.getStatus()));
    }

    private AdminBlogArticleResponse toResponse(BlogArticle article) {
        AdminBlogArticleResponse response = adminBlogMapper.toArticleResponse(article);
        BlogCategory category = categoryService.findById(article.getCategoryId()).orElse(null);
        response.setCategoryName(category != null ? category.getName() : "");
        response.setStatusLabel(article.getStatus().getLabel());
        response.setCoverImageUrl(article.getCoverImageFileId() != null
                ? webPublicUrl + "/web/file/" + article.getCoverImageFileId()
                : "");
        return response;
    }
}
