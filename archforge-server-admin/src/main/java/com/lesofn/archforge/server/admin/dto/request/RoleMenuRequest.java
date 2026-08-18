package com.lesofn.archforge.server.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * 保存角色菜单请求
 *
 * @author lesofn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleMenuRequest {

    @NotNull(message = "角色ID不能为空")
    private Long id;

    @Nullable
    private List<Long> menuIds;
}
