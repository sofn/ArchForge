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
public class DictItemRequest {

    @Nullable
    private Long id;

    @NotBlank(message = "项编码不能为空")
    private String itemCode;

    @NotBlank(message = "项名称不能为空")
    private String itemLabel;

    @Builder.Default
    private Integer sort = 0;

    @Builder.Default
    private Integer status = 1;
}
