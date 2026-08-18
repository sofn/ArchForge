package com.lesofn.archforge.server.web.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebNoticeResponse {

    private Long id;
    private String title;
    private String content;
    private Integer noticeType;
    private LocalDateTime createTime;
}
