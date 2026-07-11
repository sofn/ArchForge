package com.lesofn.archforge.server.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录日志响应
 *
 * @author lesofn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginLogResponse {

    private Long id;

    private String username;

    private String ip;

    private String address;

    private String system;

    private String browser;

    private Integer status;

    private String behavior;

    private Long loginTime;
}
