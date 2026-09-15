package com.lesofn.archforge.server.admin.controller.cms;

import com.lesofn.archforge.cms.api.domain.CmsCategory;
import com.lesofn.archforge.cms.api.service.CmsArticleService;
import com.lesofn.archforge.cms.api.service.CmsCategoryService;
import com.lesofn.archforge.server.admin.dto.AdminPageResponse;
import com.lesofn.archforge.server.admin.dto.request.AdminCmsCategoryCreateRequest;
import com.lesofn.archforge.server.admin.dto.request.AdminCmsCategoryListRequest;
import com.lesofn.archforge.server.admin.dto.request.AdminCmsCategoryUpdateRequest;
import com.lesofn.archforge.server.admin.dto.request.IdRequest;
import com.lesofn.archforge.server.admin.dto.response.AdminCmsCategoryResponse;
import com.lesofn.archforge.server.admin.convertor.AdminCmsConvertor;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/cms/category")
@SaCheckLogin(type = StpAdminUtil.TYPE)
@SaCheckRole(value = "ADMIN", type = StpAdminUtil.TYPE)
@RequiredArgsConstructor
public class CmsCategoryController {

    private final CmsCategoryService categoryService;
    private final CmsArticleService articleService;
    private final AdminCmsConvertor adminCmsMapper;

    @PostMapping
    public AdminPageResponse<AdminCmsCategoryResponse> list(@RequestBody @Valid AdminCmsCategoryListRequest request) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(request.getCurrentPage() - 1, 0),
                request.getPageSize(),
                Sort.by("sortOrder").ascending());
        Page<CmsCategory> page = categoryService.page(pageRequest, request.getName());
        List<AdminCmsCategoryResponse> list = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return AdminPageResponse.of(list, page.getTotalElements(), page.getSize(), request.getCurrentPage());
    }

    @SaCheckPermission(value = "cms:category:add", type = StpAdminUtil.TYPE)
    @PostMapping("/create")
    public Long create(@RequestBody @Valid AdminCmsCategoryCreateRequest request) {
        CmsCategory category = new CmsCategory()
                .setName(request.getName())
                .setSlug(request.getSlug())
                .setSortOrder(java.util.Objects.requireNonNullElse(request.getSortOrder(), 0))
                .setStatus(java.util.Objects.requireNonNullElse(request.getStatus(), 1));
        return categoryService.create(category).getId();
    }

    @SaCheckPermission(value = "cms:category:edit", type = StpAdminUtil.TYPE)
    @PutMapping("/update")
    public Boolean update(@RequestBody @Valid AdminCmsCategoryUpdateRequest request) {
        CmsCategory category = new CmsCategory()
                .setId(request.getId())
                .setName(request.getName())
                .setSlug(request.getSlug())
                .setSortOrder(java.util.Objects.requireNonNullElse(request.getSortOrder(), 0))
                .setStatus(java.util.Objects.requireNonNullElse(request.getStatus(), 1));
        categoryService.update(category);
        return true;
    }

    @SaCheckPermission(value = "cms:category:remove", type = StpAdminUtil.TYPE)
    @PostMapping("/delete")
    public Boolean delete(@RequestBody @Valid IdRequest request) {
        categoryService.delete(request.getId());
        return true;
    }

    private AdminCmsCategoryResponse toResponse(CmsCategory category) {
        AdminCmsCategoryResponse response = adminCmsMapper.toCategoryResponse(category);
        long articleCount = articleService.pageAll(
                PageRequest.of(0, 1, Sort.by("createTime").descending()),
                category.getId(), null, null).getTotalElements();
        response.setStatusLabel(category.isVisible() ? "显示" : "隐藏");
        response.setArticleCount(articleCount);
        return response;
    }
}
