package com.lesofn.archforge.server.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * 更新通知公告请求
 *
 * @author lesofn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeUpdateRequest {

    @NotNull(message = "通知ID不能为空")
    private Long id;

    @Nullable
    private String noticeTitle;

    @Nullable
    private Integer noticeType;

    @Nullable
    private String noticeContent;

    @Nullable
    private Integer status;

    @Nullable
    private String remark;
}
