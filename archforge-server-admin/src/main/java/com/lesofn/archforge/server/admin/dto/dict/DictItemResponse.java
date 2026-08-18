package com.lesofn.archforge.server.admin.dto.dict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictItemResponse {

    private Long id;

    private String itemCode;

    private String itemLabel;

    private Integer sort;

    private Integer status;

    @Nullable
    private Long dictTypeId;
}
