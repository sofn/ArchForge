package com.lesofn.archforge.server.admin.dto.response;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 参数配置响应
 *
 * @author lesofn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("NullAway.Init")
public class ConfigResponse {

    private @Nullable Long id;

    private @Nullable String configName;

    private @Nullable String configKey;

    private @Nullable String configValue;

    private @Nullable Integer configType;

    private @Nullable String remark;

    private @Nullable Long createTime;
}
