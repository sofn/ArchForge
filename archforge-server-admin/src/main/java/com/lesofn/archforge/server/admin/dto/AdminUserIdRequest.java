package com.lesofn.archforge.server.admin.dto;

import lombok.Data;

/**
 * 管理端用户ID请求
 *
 * @author lesofn
 */
@Data
@SuppressWarnings("NullAway.Init")
public class AdminUserIdRequest {

    /** 用户ID */
    private Long userId;
}
