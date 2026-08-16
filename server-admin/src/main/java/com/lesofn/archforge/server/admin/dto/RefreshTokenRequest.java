package com.lesofn.archforge.server.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新Token请求
 *
 * @author lesofn
 */
@Data
public class RefreshTokenRequest {

    /** 刷新令牌 */
    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;
}
