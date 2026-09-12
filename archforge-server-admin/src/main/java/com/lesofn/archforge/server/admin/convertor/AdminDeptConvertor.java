package com.lesofn.archforge.server.admin.convertor;

import com.lesofn.archforge.server.admin.dto.AdminDeptDTO;
import org.jspecify.annotations.Nullable;
import com.lesofn.archforge.user.api.domain.SysDept;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** 部门 Entity → DTO 映射 */
@Mapper(componentModel = "spring")
public interface AdminDeptConvertor {

    @Mapping(source = "deptId", target = "id")
    @Mapping(source = "createTime", target = "createTime", qualifiedByName = "toEpochMilli")
    AdminDeptDTO toDto(SysDept dept);

    @org.mapstruct.Named("toEpochMilli")
    default @Nullable Long toEpochMilli(@Nullable LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
