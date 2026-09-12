package com.lesofn.archforge.user.api.domain.query;

import lombok.Data;
import org.jspecify.annotations.Nullable;

/**
 * @author lesofn
 */
@Data
public class SysUserQuery {
    private @Nullable String username;
    private @Nullable String email;
    private @Nullable String phoneNumber;
    private @Nullable Boolean enabled;
}
