package com.lesofn.archforge.user.api.domain.query;

import lombok.Data;
import org.jspecify.annotations.Nullable;

/**
 * 用户动态查询条件。
 *
 * @author lesofn
 */
@Data
public class SysUserQuery {
    private @Nullable String username;
    private @Nullable String email;
    private @Nullable String phoneNumber;
    private @Nullable Boolean enabled;
}
