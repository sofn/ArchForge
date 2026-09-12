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
public class AdminBlogCategoryResponse {

    private @Nullable Long id;
    private @Nullable String name;
    private @Nullable String slug;
    private @Nullable Integer sortOrder;
    private @Nullable Integer status;
    private @Nullable String statusLabel;
    private @Nullable Long articleCount;
    private @Nullable LocalDateTime createTime;
}
