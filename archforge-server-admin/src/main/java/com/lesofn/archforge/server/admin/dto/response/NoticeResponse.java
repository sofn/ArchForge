package com.lesofn.archforge.server.admin.dto.response;

import lombok.AllArgsConstructor;
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
public class NoticeResponse {

    private Long id;

    private String noticeTitle;

    private Integer noticeType;

    private String noticeContent;

    private Integer status;

    private String remark;

    private Long createTime;
}
