package com.lesofn.archsmith.server.admin.mapper;

import com.lesofn.archsmith.server.admin.dto.AdminRoleMenuItemDTO;
import com.lesofn.archsmith.user.domain.SysMenu;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** 角色菜单 Entity → DTO 映射 */
@Mapper(componentModel = "spring")
public interface AdminRoleMenuMapper {

    @Mapping(source = "menuId", target = "id")
    @Mapping(
            target = "title",
            expression = "java(menu.getMetaInfo() != null ? menu.getMetaInfo().getTitle() : null)")
    AdminRoleMenuItemDTO toDto(SysMenu menu);
}
