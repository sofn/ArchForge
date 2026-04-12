package com.lesofn.appforge.user.menu.dto;

import com.lesofn.appforge.user.domain.SysMenu;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author sofn
 */
@EqualsAndHashCode(callSuper = true)
@Data
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
