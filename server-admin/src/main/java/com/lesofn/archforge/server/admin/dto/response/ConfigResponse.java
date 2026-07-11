package com.lesofn.archforge.server.admin.dto.response;

import lombok.AllArgsConstructor;
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
public class ConfigResponse {

    private Long id;

    private String configName;

    private String configKey;

    private String configValue;

    private Integer configType;

    private String remark;

    private Long createTime;
}
