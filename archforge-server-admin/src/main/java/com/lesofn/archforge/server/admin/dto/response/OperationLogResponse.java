package com.lesofn.archforge.server.admin.dto.response;

import lombok.AllArgsConstructor;
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
public class OperationLogResponse {

    private Long id;

    private String username;

    private String module;

    private String summary;

    private String ip;

    private String address;

    private String system;

    private String browser;

    private Integer status;

    private Long operatingTime;
}
