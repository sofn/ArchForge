package com.lesofn.archforge.server.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * 更新参数配置请求
 *
 * @author lesofn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigUpdateRequest {

    @NotNull(message = "参数ID不能为空")
    private Long id;

    @Nullable
    private String configName;

    @Nullable
    private String configKey;

    @Nullable
    private String configValue;

    @Nullable
    private Integer configType;

    @Nullable
    private String remark;
}
