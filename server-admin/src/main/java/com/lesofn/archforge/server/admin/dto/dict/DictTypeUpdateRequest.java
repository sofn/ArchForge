package com.lesofn.archforge.server.admin.dto.dict;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictTypeUpdateRequest {

    @NotBlank(message = "字典名称不能为空")
    private String dictName;

    @Nullable
    private String description;

    @Builder.Default
    private Integer status = 1;

    @Builder.Default
    private Integer sort = 0;
}
