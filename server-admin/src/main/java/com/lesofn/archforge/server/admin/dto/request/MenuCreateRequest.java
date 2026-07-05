package com.lesofn.archforge.server.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * 创建菜单请求
 *
 * @author lesofn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuCreateRequest {

    @Nullable
    private Long parentId;

    @Nullable
    private Integer menuType;

    @Nullable
    private String name;

    @Nullable
    private String path;

    @Nullable
    private String auths;

    @Nullable
    private Integer status;

    @Nullable
    private String title;

    @Nullable
    private String icon;

    @Nullable
    private Integer rank;

    @Nullable
    private Boolean showLink;

    @Nullable
    private Boolean showParent;

    @Nullable
    private Boolean keepAlive;

    @Nullable
    private String frameSrc;

    @Nullable
    private Boolean frameLoading;

    @Nullable
    private Boolean hiddenTag;
}
