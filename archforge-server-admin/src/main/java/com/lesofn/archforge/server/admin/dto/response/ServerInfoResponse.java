package com.lesofn.archforge.server.admin.dto.response;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务器监控信息响应
 *
 * @author lesofn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("NullAway.Init")
public class ServerInfoResponse {

    private @Nullable Object cpu;

    private @Nullable Object memory;

    private @Nullable Object jvm;

    private @Nullable Object os;

    private @Nullable Object disks;

    private @Nullable String error;
}
