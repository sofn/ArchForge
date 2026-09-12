package com.lesofn.archforge.server.admin.dto.response;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知公告响应
 *
 * @author lesofn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("NullAway.Init")
public class NoticeResponse {

    private @Nullable Long id;

    private @Nullable String noticeTitle;

    private @Nullable Integer noticeType;

    private @Nullable String noticeContent;

    private @Nullable Integer status;

    private @Nullable String remark;

    private @Nullable Long createTime;
}
