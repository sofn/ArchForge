package com.lesofn.archforge.server.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户状态请求
 *
 * @author lesofn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusRequest {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
