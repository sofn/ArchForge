package com.lesofn.archforge.server.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * 重置用户密码请求
 *
 * @author lesofn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordRequest {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    @Nullable
    @Builder.Default
    private String newPwd = "admin123";
}
