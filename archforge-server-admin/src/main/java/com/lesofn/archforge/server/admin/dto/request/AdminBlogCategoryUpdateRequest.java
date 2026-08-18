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
public class AdminBlogCategoryUpdateRequest {

    @NotNull(message = "分类ID不能为空")
    private Long id;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    @NotBlank(message = "URL标识不能为空")
    private String slug;

    private Integer sortOrder = 0;

    private Integer status = 1;
}
