package com.lesofn.archforge.server.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebCategoryResponse {

    private Long id;
    private String name;
    private String slug;
    private Integer sortOrder;
    private Long articleCount;
}
