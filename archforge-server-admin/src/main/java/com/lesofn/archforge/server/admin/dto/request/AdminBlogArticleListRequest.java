package com.lesofn.archforge.server.admin.dto.request;

import com.lesofn.archforge.server.admin.dto.BasePageRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AdminBlogArticleListRequest extends BasePageRequest {

    private Long categoryId;
    private String title;
    private Integer status;
}
