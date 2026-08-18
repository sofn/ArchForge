package com.lesofn.archforge.server.admin.dto.dict;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictTypeResponse {

    private Long id;

    private String dictCode;

    private String dictName;

    @Nullable
    private String description;

    private Integer status;

    private Integer sort;

    @Nullable
    private LocalDateTime createTime;

    @Nullable
    private LocalDateTime updateTime;

    @Nullable
    private List<DictItemResponse> items;
}
