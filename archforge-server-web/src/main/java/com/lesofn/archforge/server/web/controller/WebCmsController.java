package com.lesofn.archforge.server.web.controller;

import com.lesofn.archforge.cms.api.domain.CmsArticle;
import org.jspecify.annotations.Nullable;
import com.lesofn.archforge.cms.api.enums.CmsArticleStatus;
import com.lesofn.archforge.cms.api.domain.CmsCategory;
import com.lesofn.archforge.cms.api.service.CmsArticleService;
import com.lesofn.archforge.cms.api.service.CmsCategoryService;
import com.lesofn.archforge.infrastructure.auth.LoginContext;
import com.lesofn.archforge.server.web.dto.WebArticleCreateRequest;
import com.lesofn.archforge.server.web.dto.WebArticleDetailResponse;
import com.lesofn.archforge.server.web.dto.WebArticleSummaryResponse;
import com.lesofn.archforge.server.web.dto.WebCategoryResponse;
import com.lesofn.archforge.server.web.dto.WebPageResponse;
import com.lesofn.archforge.user.api.domain.SysFile;
import com.lesofn.archforge.user.api.service.SysFileService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/web")
@RequiredArgsConstructor
public class WebCmsController {

    private final CmsCategoryService categoryService;
    private final CmsArticleService articleService;
    private final SysFileService sysFileService;

    @Value("${arch-forge.web.public-url:http://localhost:8081}")
    private String webPublicUrl;

    @GetMapping("/categories")
    public List<WebCategoryResponse> getCategories() {
        List<CmsCategory> categories = categoryService.listVisible();
        return categories.stream().map(c -> {
            long count = articleService.pagePublished(
                    PageRequest.of(0, 1, Sort.by("publishTime").descending()),
                    c.getId(), null).getTotalElements();
            return new WebCategoryResponse(c.getId(), c.getName(), c.getSlug(), c.getSortOrder(), count);
        }).collect(Collectors.toList());
    }

    @GetMapping("/articles")
    public WebPageResponse<WebArticleSummaryResponse> getArticles(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "1") int currentPage,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(currentPage - 1, 0),
                Math.max(pageSize, 1),
                Sort.by("publishTime").descending());
        Page<CmsArticle> page = articleService.pagePublished(pageRequest, categoryId, keyword);
        List<WebArticleSummaryResponse> list = page.getContent().stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
        return new WebPageResponse<>(list, page.getTotalElements(), page.getSize(), currentPage);
    }

    @GetMapping("/articles/{slug}")
    public WebArticleDetailResponse getArticle(@PathVariable String slug) {
        CmsArticle article = articleService.findPublishedBySlug(slug)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        CmsCategory category = categoryService.findById(article.getCategoryId()).orElse(null);
        return toDetailResponse(article, category);
    }

    @PostMapping("/articles")
    public Long createArticle(@RequestBody @Valid WebArticleCreateRequest request) {
        Long authorId = java.util.Objects.requireNonNull(LoginContext.getWebUserId());
        String baseSlug = request.getTitle().trim()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-zA-Z0-9-]", "")
                .toLowerCase(Locale.ROOT);
        if (baseSlug.isBlank()) {
            baseSlug = "article";
        }
        String slug = baseSlug + "-" + System.currentTimeMillis();
        CmsArticle article = new CmsArticle()
                .setCategoryId(request.getCategoryId())
                .setTitle(request.getTitle())
                .setSlug(slug)
                .setSummary(request.getSummary())
                .setContent(request.getContent())
                .setCoverImageFileId(request.getCoverImageFileId())
                .setAuthorId(authorId)
                .setStatus(CmsArticleStatus.PUBLISHED);
        return articleService.create(article).getId();
    }

    @GetMapping("/user/articles")
    public WebPageResponse<WebArticleSummaryResponse> myArticles(
            @RequestParam(required = false, defaultValue = "1") int currentPage,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        Long authorId = java.util.Objects.requireNonNull(LoginContext.getWebUserId());
        PageRequest pageRequest = PageRequest.of(
                Math.max(currentPage - 1, 0),
                Math.max(pageSize, 1),
                Sort.by("createTime").descending());
        Page<CmsArticle> page = articleService.pageByAuthorId(pageRequest, authorId);
        List<WebArticleSummaryResponse> list = page.getContent().stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
        return new WebPageResponse<>(list, page.getTotalElements(), page.getSize(), currentPage);
    }

    private WebArticleSummaryResponse toSummaryResponse(CmsArticle article) {
        String coverUrl = buildFileUrl(article.getCoverImageFileId());
        String categoryName = categoryService.findById(article.getCategoryId())
                .map(CmsCategory::getName).orElse("");
        return new WebArticleSummaryResponse(article.getId(), article.getTitle(), article.getSlug(), article
                .getSummary(), article.getCoverImageFileId(), coverUrl, categoryName, article.getPublishTime());
    }

    private WebArticleDetailResponse toDetailResponse(CmsArticle article, @Nullable CmsCategory category) {
        String coverUrl = buildFileUrl(article.getCoverImageFileId());
        return new WebArticleDetailResponse(article.getId(), article.getTitle(), article.getSlug(), article
                .getSummary(), article.getContent(), article.getCoverImageFileId(), coverUrl, category != null ? category
                        .getId() : null, category != null ? category.getName() : "", category != null ? category.getSlug()
                                : "", article.getPublishTime(), article.getCreateTime());
    }

    private String buildFileUrl(Long fileId) {
        if (fileId == null) {
            return "";
        }
        Optional<SysFile> fileOpt = sysFileService.findById(fileId);
        return fileOpt.isPresent() ? webPublicUrl + "/web/file/" + fileId : "";
    }
}
