package com.lesofn.archforge.server.admin.mapper;

import com.lesofn.archforge.common.enums.common.GenderEnum;
import com.lesofn.archforge.server.admin.dto.AdminUserItemDTO;
import com.lesofn.archforge.server.admin.dto.request.UserCreateRequest;
import com.lesofn.archforge.user.api.domain.SysUser;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/** 用户 Entity ↔ DTO 映射 */
@Mapper(componentModel = "spring")
public interface AdminUserMapper {

    @Mapping(source = "userId", target = "id")
    @Mapping(source = "phoneNumber", target = "phone")
    @Mapping(source = "sex", target = "sex", qualifiedByName = "genderToInt")
    @Mapping(source = "createTime", target = "createTime", qualifiedByName = "toEpochMilli")
    @Mapping(target = "dept", ignore = true)
    AdminUserItemDTO toDto(SysUser user, @Context Map<Long, String> deptNameMap);

    @Mapping(source = "parentId", target = "deptId")
    @Mapping(source = "phone", target = "phoneNumber")
    @Mapping(source = "sex", target = "sex", qualifiedByName = "intToGender")
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "roleId", ignore = true)
    @Mapping(target = "userType", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "loginIp", ignore = true)
    @Mapping(target = "loginDate", ignore = true)
    @Mapping(target = "isAdmin", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "creatorId", ignore = true)
    @Mapping(target = "updaterId", ignore = true)
    SysUser fromCreateRequest(UserCreateRequest request);

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

    @Named("genderToInt")
    default Integer genderToInt(GenderEnum gender) {
        return gender != null ? gender.getValue() : null;
    }

    @Named("intToGender")
    default GenderEnum intToGender(Integer value) {
        return value == null ? null : GenderEnum.fromValue(value);
    }

    @Named("toEpochMilli")
    default Long toEpochMilli(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
