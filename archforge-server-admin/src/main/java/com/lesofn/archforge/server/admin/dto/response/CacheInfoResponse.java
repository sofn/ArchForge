package com.lesofn.archforge.server.admin.dto.response;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Redis 缓存监控信息响应
 *
 * @author lesofn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("NullAway.Init")
public class CacheInfoResponse {

    private @Nullable Long dbSize;

    private @Nullable String usedMemory;

    private @Nullable String usedMemoryHuman;

    private @Nullable String connectedClients;

    private @Nullable String instantaneousOpsPerSec;

    private @Nullable String totalCommandsProcessed;

    private @Nullable String keyspaceHits;

    private @Nullable String keyspaceMisses;

    private @Nullable String info;
}
