package com.lesofn.archforge.server.admin.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminBlogCategoryResponse {

    private Long id;
    private String name;
    private String slug;
    private Integer sortOrder;
    private Integer status;
    private String statusLabel;
    private Long articleCount;
    private LocalDateTime createTime;
}
