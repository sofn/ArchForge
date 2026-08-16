package com.lesofn.archforge.server.admin.mapper;

import com.lesofn.archforge.server.admin.dto.AdminRoleDTO;
import com.lesofn.archforge.user.api.domain.SysRole;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/** 角色 Entity → DTO 映射 */
@Mapper(componentModel = "spring")
public interface AdminRoleConvertor {

    @Mapping(source = "roleId", target = "id")
    @Mapping(source = "roleName", target = "name")
    @Mapping(source = "roleKey", target = "code")
    @Mapping(
            target = "status",
            expression = "java(role.getStatus() != null ? role.getStatus().intValue() : null)")
    @Mapping(
            target = "dataScope",
            expression = "java(role.getDataScope() != null ? role.getDataScope().intValue() : null)")
    @Mapping(target = "customDeptIds", source = "deptIdSet", qualifiedByName = "toLongList")
    @Mapping(source = "createTime", target = "createTime", qualifiedByName = "toEpochMilli")
    @Mapping(source = "updateTime", target = "updateTime", qualifiedByName = "toEpochMilli")
    AdminRoleDTO toDto(SysRole role);

    @Named("toEpochMilli")
    default Long toEpochMilli(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    @Named("toLongList")
    default List<Long> toLongList(@Nullable String deptIdSet) {
        if (deptIdSet == null || deptIdSet.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(deptIdSet.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    default String toDeptIdSet(List<Long> customDeptIds) {
        if (customDeptIds == null || customDeptIds.isEmpty()) {
            return "";
        }
        return customDeptIds.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
