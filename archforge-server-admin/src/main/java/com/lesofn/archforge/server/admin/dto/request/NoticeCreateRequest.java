package com.lesofn.archforge.server.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * 创建通知公告请求
 *
 * @author lesofn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeCreateRequest {

    @NotBlank(message = "通知标题不能为空")
    private String noticeTitle;

    @Nullable
    @Builder.Default
    private Integer noticeType = 1;

    @Nullable
    private String noticeContent;

    @Nullable
    @Builder.Default
    private Integer status = 1;

    @Nullable
    private String remark;
}
