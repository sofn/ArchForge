package com.lesofn.archforge.server.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * 创建部门请求
 *
 * @author lesofn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeptCreateRequest {

    @Nullable
    @Builder.Default
    private Long parentId = 0L;

    @NotBlank(message = "部门名称不能为空")
    private String name;

    @Nullable
    private String principal;

    @Nullable
    private String phone;

    @Nullable
    private String email;

    @Nullable
    @Builder.Default
    private Integer sort = 0;

    @Nullable
    @Builder.Default
    private Integer status = 1;

    @Nullable
    private String remark;
}
