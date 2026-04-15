package com.lesofn.archsmith.server.admin.mapper;

import com.lesofn.archsmith.server.admin.dto.AdminDeptItemDTO;
import com.lesofn.archsmith.user.domain.SysDept;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** 部门 Entity → DTO 映射 */
@Mapper(componentModel = "spring")
public interface AdminDeptMapper {

    @Mapping(source = "deptId", target = "id")
    @Mapping(source = "createTime", target = "createTime", qualifiedByName = "toEpochMilli")
    AdminDeptItemDTO toDto(SysDept dept);

    @org.mapstruct.Named("toEpochMilli")
    default Long toEpochMilli(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
