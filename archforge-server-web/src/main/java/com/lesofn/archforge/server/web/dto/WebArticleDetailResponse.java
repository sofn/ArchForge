package com.lesofn.archforge.server.web.dto;

import java.time.LocalDateTime;
import org.jspecify.annotations.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("NullAway.Init")
public class WebArticleDetailResponse {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private Long coverImageFileId;
    private String coverImageUrl;
    private @Nullable Long categoryId;
    private @Nullable String categoryName;
    private @Nullable String categorySlug;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
}
