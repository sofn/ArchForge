package com.lesofn.archforge.server.admin.dto.dict;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class DictTypeCreateRequest {

    @NotBlank(message = "字典编码不能为空")
    private String dictCode;

    @NotBlank(message = "字典名称不能为空")
    private String dictName;

    @Nullable
    private String description;

    @Builder.Default
    private Integer status = 1;

    @Builder.Default
    private Integer sort = 0;

    @Nullable
    @Valid
    private List<DictItemRequest> items;
}
