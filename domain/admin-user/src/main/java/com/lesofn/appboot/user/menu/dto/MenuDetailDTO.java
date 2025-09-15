package com.lesofn.appboot.user.menu.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lesofn.appboot.user.domain.SysMenu;
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
            ObjectMapper objectMapper = new ObjectMapper();
            this.meta = objectMapper.convertValue(entity.getMetaInfo(), MetaDTO.class);
        }
        this.permission = entity.getPermission();
    }

    private String permission;
    private MetaDTO meta;

}
