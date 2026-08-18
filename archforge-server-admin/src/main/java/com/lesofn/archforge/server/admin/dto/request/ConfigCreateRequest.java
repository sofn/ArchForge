package com.lesofn.archforge.server.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * 创建参数配置请求
 *
 * @author lesofn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigCreateRequest {

    @NotBlank(message = "参数名称不能为空")
    private String configName;

    @NotBlank(message = "参数键名不能为空")
    private String configKey;

    @Nullable
    private String configValue;

    @Nullable
    @Builder.Default
    private Integer configType = 0;

    @Nullable
    private String remark;
}
