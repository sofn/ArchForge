package com.lesofn.archforge.server.admin.dto.response;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 操作日志响应
 *
 * @author lesofn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("NullAway.Init")
public class OperationLogResponse {

    private @Nullable Long id;

    private @Nullable String username;

    private @Nullable String module;

    private @Nullable String summary;

    private @Nullable String ip;

    private @Nullable String address;

    private @Nullable String system;

    private @Nullable String browser;

    private @Nullable Integer status;

    private @Nullable Long operatingTime;
}
