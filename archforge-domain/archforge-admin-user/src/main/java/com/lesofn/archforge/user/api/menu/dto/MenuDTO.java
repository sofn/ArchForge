package com.lesofn.archforge.user.api.menu.dto;

import com.lesofn.archforge.common.enums.BasicEnumUtil;
import com.lesofn.archforge.common.enums.common.StatusEnum;
import com.lesofn.archforge.user.api.domain.SysMenu;
import com.lesofn.archforge.user.api.enums.MenuTypeEnum;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author sofn
 */
@Data
@NoArgsConstructor
public class MenuDTO {

    public MenuDTO(SysMenu entity) {
        if (entity != null) {
            this.id = entity.getMenuId();
            this.parentId = entity.getParentId();
            this.menuName = entity.getMenuName();
            this.routerName = entity.getRouterName();
            this.path = entity.getPath();
            this.status = entity.getStatus();
            this.isButton = entity.getIsButton();
            this.statusStr = BasicEnumUtil.getDescriptionByValue(StatusEnum.class, entity.getStatus());

            this.menuType = entity.getMenuType();
            this.menuTypeStr = Boolean.TRUE.equals(entity.getIsButton())
                    ? "按钮"
                    : BasicEnumUtil.getDescriptionByValue(MenuTypeEnum.class, entity.getMenuType());

            if (entity.getMetaInfo() != null) {
                MetaDTO meta = entity.getMetaInfo();
                this.rank = meta.getRank();
                this.icon = meta.getIcon();
            }
            this.createTime = entity.getCreateTime();
        }
    }

    // 设置成id和parentId 便于前端处理树级结构
    private Long id;

    private Long parentId;

    private String menuName;

    private String routerName;

    private String path;

    private Integer rank;

    private Integer menuType;

    private String menuTypeStr;

    private Boolean isButton;

    private Integer status;

    private String statusStr;

    private LocalDateTime createTime;

    private String icon;
}
