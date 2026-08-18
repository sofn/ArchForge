package com.lesofn.archforge.server.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * 分配用户角色请求
 *
 * @author lesofn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleRequest {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    @Nullable
    private List<Long> ids;
}
