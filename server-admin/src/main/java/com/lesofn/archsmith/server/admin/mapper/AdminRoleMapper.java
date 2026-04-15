package com.lesofn.archsmith.server.admin.mapper;

import com.lesofn.archsmith.server.admin.dto.AdminRoleItemDTO;
import com.lesofn.archsmith.user.domain.SysRole;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** 角色 Entity → DTO 映射 */
@Mapper(componentModel = "spring")
public interface AdminRoleMapper {

    @Mapping(source = "roleId", target = "id")
    @Mapping(source = "roleName", target = "name")
    @Mapping(source = "roleKey", target = "code")
    @Mapping(
            target = "status",
            expression = "java(role.getStatus() != null ? role.getStatus().intValue() : null)")
    @Mapping(source = "createTime", target = "createTime", qualifiedByName = "toEpochMilli")
    @Mapping(source = "updateTime", target = "updateTime", qualifiedByName = "toEpochMilli")
    AdminRoleItemDTO toDto(SysRole role);

    @org.mapstruct.Named("toEpochMilli")
    default Long toEpochMilli(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
