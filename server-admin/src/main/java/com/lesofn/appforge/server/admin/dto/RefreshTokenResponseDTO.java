package com.lesofn.appforge.server.admin.dto;

import lombok.Data;

/**
 * 刷新Token响应DTO
 *
 * @author lesofn
 */
@Data
public class RefreshTokenResponseDTO {

    /** 访问令牌 */
    private String accessToken;

    /** 刷新令牌 */
    private String refreshToken;

    /** 过期时间 格式: yyyy/MM/dd HH:mm:ss */
    private String expires;
}
