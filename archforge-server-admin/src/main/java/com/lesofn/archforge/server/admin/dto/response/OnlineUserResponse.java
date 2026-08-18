package com.lesofn.archforge.server.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 在线用户信息响应
 *
 * @author lesofn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineUserResponse {

    private String id;

    private String username;

    private String ip;

    private String address;

    private String system;

    private String browser;

    private Long loginTime;
}
