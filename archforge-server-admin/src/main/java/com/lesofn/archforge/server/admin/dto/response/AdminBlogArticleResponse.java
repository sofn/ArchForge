package com.lesofn.archforge.server.admin.dto.response;

import java.time.LocalDateTime;
import org.jspecify.annotations.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("NullAway.Init")
public class AdminBlogArticleResponse {

    private @Nullable Long id;
    private @Nullable Long categoryId;
    private @Nullable String categoryName;
    private @Nullable String title;
    private @Nullable String slug;
    private @Nullable String summary;
    private @Nullable String content;
    private @Nullable Long coverImageFileId;
    private @Nullable String coverImageUrl;
    private @Nullable Integer status;
    private @Nullable String statusLabel;
    private @Nullable LocalDateTime publishTime;
    private @Nullable LocalDateTime createTime;
    private @Nullable LocalDateTime updateTime;
}
