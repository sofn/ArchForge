package com.lesofn.archforge.server.admin.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminBlogArticleResponse {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private Long coverImageFileId;
    private String coverImageUrl;
    private Integer status;
    private String statusLabel;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
