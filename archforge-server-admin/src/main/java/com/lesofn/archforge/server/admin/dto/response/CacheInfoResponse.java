package com.lesofn.archforge.server.admin.dto.response;

import lombok.AllArgsConstructor;
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
public class CacheInfoResponse {

    private Long dbSize;

    private String usedMemory;

    private String usedMemoryHuman;

    private String connectedClients;

    private String instantaneousOpsPerSec;

    private String totalCommandsProcessed;

    private String keyspaceHits;

    private String keyspaceMisses;

    private String info;
}
