package com.lesofn.archforge.server.admin.convertor;

import com.lesofn.archforge.cms.api.domain.CmsArticle;
import com.lesofn.archforge.cms.api.domain.CmsCategory;
import com.lesofn.archforge.server.admin.dto.response.AdminCmsArticleResponse;
import com.lesofn.archforge.server.admin.dto.response.AdminCmsCategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminCmsConvertor {

    @Mapping(target = "statusLabel", ignore = true)
    @Mapping(target = "articleCount", ignore = true)
    AdminCmsCategoryResponse toCategoryResponse(CmsCategory category);

    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "statusLabel", ignore = true)
    @Mapping(target = "coverImageUrl", ignore = true)
    AdminCmsArticleResponse toArticleResponse(CmsArticle article);
}
