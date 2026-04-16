package com.lesofn.archsmith.server.admin.mapper;

import com.lesofn.archsmith.server.admin.dto.AdminMenuItemDTO;
import com.lesofn.archsmith.user.domain.SysMenu;
import com.lesofn.archsmith.user.menu.dto.ExtraIconDTO;
import com.lesofn.archsmith.user.menu.dto.MetaDTO;
import com.lesofn.archsmith.user.menu.dto.TransitionDTO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/** 菜单 Entity → DTO 映射 */
@Mapper(componentModel = "spring")
public interface AdminMenuMapper {

    @Mapping(source = "menuId", target = "id")
    @Mapping(source = "routerName", target = "name")
    @Mapping(
            target = "auths",
            expression = "java(menu.getPermission() != null ? menu.getPermission() : \"\")")
    @Mapping(target = "component", constant = "")
    @Mapping(target = "redirect", constant = "")
    @Mapping(target = "activePath", constant = "")
    @Mapping(target = "fixedTag", constant = "false")
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "rank", ignore = true)
    @Mapping(target = "icon", ignore = true)
    @Mapping(target = "extraIcon", ignore = true)
    @Mapping(target = "enterTransition", ignore = true)
    @Mapping(target = "leaveTransition", ignore = true)
    @Mapping(target = "frameSrc", ignore = true)
    @Mapping(target = "frameLoading", ignore = true)
    @Mapping(target = "keepAlive", ignore = true)
    @Mapping(target = "hiddenTag", ignore = true)
    @Mapping(target = "showLink", ignore = true)
    @Mapping(target = "showParent", ignore = true)
    AdminMenuItemDTO toDto(SysMenu menu);

    @AfterMapping
    default void mapMetaInfo(SysMenu menu, @MappingTarget AdminMenuItemDTO dto) {
        MetaDTO meta = menu.getMetaInfo();
        if (meta != null) {
            dto.setTitle(meta.getTitle());
            dto.setRank(meta.getRank());
            dto.setIcon(meta.getIcon() != null ? meta.getIcon() : "");
            dto.setFrameSrc(meta.getFrameSrc() != null ? meta.getFrameSrc() : "");
            dto.setFrameLoading(meta.getFrameLoading() != null ? meta.getFrameLoading() : true);
            dto.setKeepAlive(meta.getKeepAlive() != null ? meta.getKeepAlive() : false);
            dto.setHiddenTag(meta.getHiddenTag() != null ? meta.getHiddenTag() : false);
            dto.setShowLink(meta.getShowLink() != null ? meta.getShowLink() : true);
            dto.setShowParent(meta.getShowParent() != null ? meta.getShowParent() : false);

            ExtraIconDTO extraIcon = meta.getExtraIcon();
            dto.setExtraIcon(extraIcon != null ? extraIcon.getName() : "");

            TransitionDTO transition = meta.getTransition();
            if (transition != null) {
                dto.setEnterTransition(
                        transition.getEnterTransition() != null
                                ? transition.getEnterTransition()
                                : "");
                dto.setLeaveTransition(
                        transition.getLeaveTransition() != null
                                ? transition.getLeaveTransition()
                                : "");
            } else {
                dto.setEnterTransition("");
                dto.setLeaveTransition("");
            }
        } else {
            dto.setTitle("");
            dto.setRank(null);
            dto.setIcon("");
            dto.setFrameSrc("");
            dto.setFrameLoading(true);
            dto.setKeepAlive(false);
            dto.setHiddenTag(false);
            dto.setShowLink(true);
            dto.setShowParent(false);
            dto.setExtraIcon("");
            dto.setEnterTransition("");
            dto.setLeaveTransition("");
        }
    }
}
