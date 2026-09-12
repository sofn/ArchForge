package com.lesofn.archforge.server.admin.dto.response;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
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
@SuppressWarnings("NullAway.Init")
public class OnlineUserResponse {

    private @Nullable String id;

    private @Nullable String username;

    private @Nullable String ip;

    private @Nullable String address;

    private @Nullable String system;

    private @Nullable String browser;

    private @Nullable Long loginTime;
}
