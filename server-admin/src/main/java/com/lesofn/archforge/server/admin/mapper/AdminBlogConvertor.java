package com.lesofn.archforge.server.admin.mapper;

import com.lesofn.archforge.blog.api.domain.BlogArticle;
import com.lesofn.archforge.blog.api.domain.BlogCategory;
import com.lesofn.archforge.server.admin.dto.response.AdminBlogArticleResponse;
import com.lesofn.archforge.server.admin.dto.response.AdminBlogCategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminBlogConvertor {

    @Mapping(target = "statusLabel", ignore = true)
    @Mapping(target = "articleCount", ignore = true)
    AdminBlogCategoryResponse toCategoryResponse(BlogCategory category);

    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "statusLabel", ignore = true)
    @Mapping(target = "coverImageUrl", ignore = true)
    AdminBlogArticleResponse toArticleResponse(BlogArticle article);
}
