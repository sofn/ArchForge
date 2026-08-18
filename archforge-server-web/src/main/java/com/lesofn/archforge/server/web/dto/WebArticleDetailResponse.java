package com.lesofn.archforge.server.web.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebArticleDetailResponse {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private Long coverImageFileId;
    private String coverImageUrl;
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
}
