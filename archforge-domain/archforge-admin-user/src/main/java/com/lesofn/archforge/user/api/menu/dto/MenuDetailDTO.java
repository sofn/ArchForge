package com.lesofn.archforge.user.api.menu.dto;

import com.lesofn.archforge.user.api.domain.SysMenu;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单详情 DTO。
 *
 * @author sofn
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuppressWarnings("NullAway.Init") // fields populated via setters (Jackson)
public class MenuDetailDTO extends MenuDTO {

    public MenuDetailDTO(SysMenu entity) {
        super(entity);
        if (entity == null) {
            return;
        }
        if (entity.getMetaInfo() != null) {
            this.meta = entity.getMetaInfo();
        }
        this.permission = entity.getPermission();
    }

    private String permission;
    private MetaDTO meta;
}
