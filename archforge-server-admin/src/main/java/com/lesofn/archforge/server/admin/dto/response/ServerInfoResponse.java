package com.lesofn.archforge.server.admin.dto.response;

import lombok.AllArgsConstructor;
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
public class ServerInfoResponse {

    private Object cpu;

    private Object memory;

    private Object jvm;

    private Object os;

    private Object disks;

    private String error;
}
