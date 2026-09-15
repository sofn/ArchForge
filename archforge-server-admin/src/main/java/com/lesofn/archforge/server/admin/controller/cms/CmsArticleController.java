package com.lesofn.archforge.server.admin.controller.cms;

import com.lesofn.archforge.cms.api.domain.CmsArticle;
import org.jspecify.annotations.Nullable;
import com.lesofn.archforge.cms.api.domain.CmsCategory;
import com.lesofn.archforge.cms.api.enums.CmsArticleStatus;
import com.lesofn.archforge.cms.api.service.CmsArticleService;
import com.lesofn.archforge.cms.api.service.CmsCategoryService;
import com.lesofn.archforge.infrastructure.auth.LoginContext;
import com.lesofn.archforge.common.auth.SystemLoginUser;
import com.lesofn.archforge.server.admin.dto.AdminPageResponse;
import com.lesofn.archforge.server.admin.dto.request.AdminCmsArticleCreateRequest;
import com.lesofn.archforge.server.admin.dto.request.AdminCmsArticleListRequest;
import com.lesofn.archforge.server.admin.dto.request.AdminCmsArticleUpdateRequest;
import com.lesofn.archforge.server.admin.dto.request.IdRequest;
import com.lesofn.archforge.server.admin.dto.response.AdminCmsArticleResponse;
import com.lesofn.archforge.server.admin.convertor.AdminCmsConvertor;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/cms/article")
@SaCheckLogin(type = StpAdminUtil.TYPE)
@SaCheckRole(value = "ADMIN", type = StpAdminUtil.TYPE)
@RequiredArgsConstructor
public class CmsArticleController {

    private final CmsArticleService articleService;
    private final CmsCategoryService categoryService;
    private final AdminCmsConvertor adminCmsMapper;

    @Value("${arch-forge.web.public-url:http://localhost:8081}")
    private String webPublicUrl;

    @PostMapping
    public AdminPageResponse<AdminCmsArticleResponse> list(@RequestBody @Valid AdminCmsArticleListRequest request) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(request.getCurrentPage() - 1, 0),
                request.getPageSize(),
                Sort.by("createTime").descending());
        CmsArticleStatus status = toStatus(request.getStatus());
        Page<CmsArticle> page = articleService.pageAll(
                pageRequest, request.getCategoryId(), request.getTitle(), status);
        List<AdminCmsArticleResponse> list = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return AdminPageResponse.of(list, page.getTotalElements(), page.getSize(), request.getCurrentPage());
    }

    @SaCheckPermission(value = "cms:article:add", type = StpAdminUtil.TYPE)
    @PostMapping("/create")
    public Long create(@RequestBody @Valid AdminCmsArticleCreateRequest request) {
        SystemLoginUser loginUser = LoginContext.getAdminUser();
        CmsArticle article = buildFromRequest(request)
                .setAuthorId(loginUser.getUserId());
        return articleService.create(article).getId();
    }

    @SaCheckPermission(value = "cms:article:edit", type = StpAdminUtil.TYPE)
    @PutMapping("/update")
    public Boolean update(@RequestBody @Valid AdminCmsArticleUpdateRequest request) {
        CmsArticle article = buildFromRequest(request).setId(request.getId());
        articleService.update(article);
        return true;
    }

    @SaCheckPermission(value = "cms:article:remove", type = StpAdminUtil.TYPE)
    @PostMapping("/delete")
    public Boolean delete(@RequestBody @Valid IdRequest request) {
        articleService.delete(request.getId());
        return true;
    }

    @SaCheckPermission(value = "cms:article:publish", type = StpAdminUtil.TYPE)
    @PostMapping("/{id}/publish")
    public Boolean publish(@PathVariable Long id) {
        articleService.publish(id);
        return true;
    }

    @SaCheckPermission(value = "cms:article:offline", type = StpAdminUtil.TYPE)
    @PostMapping("/{id}/offline")
    public Boolean offline(@PathVariable Long id) {
        articleService.offline(id);
        return true;
    }

    private @Nullable CmsArticleStatus toStatus(@Nullable Integer value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case 0 -> CmsArticleStatus.DRAFT;
            case 1 -> CmsArticleStatus.PUBLISHED;
            case 2 -> CmsArticleStatus.OFFLINE;
            default -> throw new IllegalArgumentException("状态值无效: " + value);
        };
    }

    private CmsArticle buildFromRequest(AdminCmsArticleCreateRequest request) {
        return new CmsArticle()
                .setCategoryId(request.getCategoryId())
                .setTitle(request.getTitle())
                .setSlug(request.getSlug())
                .setSummary(request.getSummary())
                .setContent(request.getContent())
                .setCoverImageFileId(request.getCoverImageFileId())
                .setStatus(java.util.Objects.requireNonNullElse(toStatus(request.getStatus()), CmsArticleStatus.DRAFT));
    }

    private CmsArticle buildFromRequest(AdminCmsArticleUpdateRequest request) {
        return new CmsArticle()
                .setCategoryId(request.getCategoryId())
                .setTitle(request.getTitle())
                .setSlug(request.getSlug())
                .setSummary(request.getSummary())
                .setContent(request.getContent())
                .setCoverImageFileId(request.getCoverImageFileId())
                .setStatus(java.util.Objects.requireNonNullElse(toStatus(request.getStatus()), CmsArticleStatus.DRAFT));
    }

    private AdminCmsArticleResponse toResponse(CmsArticle article) {
        AdminCmsArticleResponse response = adminCmsMapper.toArticleResponse(article);
        CmsCategory category = categoryService.findById(article.getCategoryId()).orElse(null);
        response.setCategoryName(category != null ? category.getName() : "");
        response.setStatusLabel(article.getStatus().getLabel());
        response.setCoverImageUrl(article.getCoverImageFileId() != null
                ? webPublicUrl + "/web/file/" + article.getCoverImageFileId()
                : "");
        return response;
    }
}
