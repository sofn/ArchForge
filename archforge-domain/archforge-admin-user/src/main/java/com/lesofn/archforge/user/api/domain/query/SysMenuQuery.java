package com.lesofn.archforge.user.api.domain.query;

import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
public class SysMenuQuery {
    private @Nullable String menuName;
    private @Nullable Short menuType;
    private @Nullable Long parentId;
    private @Nullable Boolean isButton;
    private @Nullable String permission;
    private @Nullable Short status;
}
