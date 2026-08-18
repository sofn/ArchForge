package com.lesofn.archforge.server.admin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * 创建用户请求
 *
 * @author lesofn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @Nullable
    private String phone;

    @Nullable
    @Email(message = "邮箱格式不正确")
    private String email;

    @Nullable
    private Integer sex;

    @NotNull(message = "状态不能为空")
    @Builder.Default
    private Integer status = 1;

    @Nullable
    private String remark;

    @Nullable
    private Long parentId;

    @NotBlank(message = "密码不能为空")
    @Builder.Default
    private String password = "admin123";
}
