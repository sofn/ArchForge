package com.lesofn.archforge.server.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBlogArticleCreateRequest {

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotBlank(message = "文章标题不能为空")
    private String title;

    @NotBlank(message = "URL标识不能为空")
    private String slug;

    private String summary;

    @NotBlank(message = "文章内容不能为空")
    private String content;

    private Long coverImageFileId;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
