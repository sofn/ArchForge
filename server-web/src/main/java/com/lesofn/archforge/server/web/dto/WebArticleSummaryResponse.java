package com.lesofn.archforge.server.web.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebArticleSummaryResponse {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private Long coverImageFileId;
    private String coverImageUrl;
    private String categoryName;
    private LocalDateTime publishTime;
}
