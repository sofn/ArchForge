package com.lesofn.archforge.server.web.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebPageResponse<T> {

    private List<T> list;
    private long total;
    private int pageSize;
    private int currentPage;
}
