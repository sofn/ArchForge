package com.lesofn.archforge.user.api.domain.query;

import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
public class SysRoleQuery {
    private @Nullable String roleName;
    private @Nullable String roleKey;
    private @Nullable Short status;
}
