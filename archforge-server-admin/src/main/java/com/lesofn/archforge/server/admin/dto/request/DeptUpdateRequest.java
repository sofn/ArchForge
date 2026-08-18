package com.lesofn.archforge.server.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * 更新部门请求
 *
 * @author lesofn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeptUpdateRequest {

    @NotNull(message = "部门ID不能为空")
    private Long id;

    @Nullable
    private Long parentId;

    @Nullable
    private String name;

    @Nullable
    private String principal;

    @Nullable
    private String phone;

    @Nullable
    private String email;

    @Nullable
    private Integer sort;

    @Nullable
    private Integer status;

    @Nullable
    private String remark;
}
