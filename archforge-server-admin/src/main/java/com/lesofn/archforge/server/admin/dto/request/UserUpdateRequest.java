package com.lesofn.archforge.server.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * 更新用户请求
 *
 * @author lesofn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    @Nullable
    private String username;

    @Nullable
    private String nickname;

    @Nullable
    private String phone;

    @Nullable
    private String email;

    @Nullable
    private Integer sex;

    @Nullable
    private Integer status;

    @Nullable
    private String remark;

    @Nullable
    private Long parentId;
}
