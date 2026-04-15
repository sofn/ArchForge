package com.lesofn.archsmith.server.admin.mapper;

import com.lesofn.archsmith.common.enums.common.GenderEnum;
import com.lesofn.archsmith.server.admin.dto.AdminUserItemDTO;
import com.lesofn.archsmith.user.domain.SysUser;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/** 用户 Entity → DTO 映射 */
@Mapper(componentModel = "spring")
public interface AdminUserMapper {

    @Mapping(source = "userId", target = "id")
    @Mapping(source = "phoneNumber", target = "phone")
    @Mapping(source = "sex", target = "sex", qualifiedByName = "genderToInt")
    @Mapping(source = "createTime", target = "createTime", qualifiedByName = "toEpochMilli")
    @Mapping(target = "dept", ignore = true)
    AdminUserItemDTO toDto(SysUser user, @Context Map<Long, String> deptNameMap);

    @AfterMapping
    default void mapDeptInfo(
            SysUser user,
            @MappingTarget AdminUserItemDTO dto,
            @Context Map<Long, String> deptNameMap) {
        if (user.getDeptId() != null) {
            String deptName = deptNameMap.getOrDefault(user.getDeptId(), "");
            dto.setDept(AdminUserItemDTO.DeptInfo.of(user.getDeptId(), deptName));
        }
    }

    @org.mapstruct.Named("genderToInt")
    default Integer genderToInt(GenderEnum gender) {
        return gender != null ? gender.getValue() : null;
    }

    @org.mapstruct.Named("toEpochMilli")
    default Long toEpochMilli(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
