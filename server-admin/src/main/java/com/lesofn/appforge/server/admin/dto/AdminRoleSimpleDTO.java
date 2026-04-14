package com.lesofn.appforge.server.admin.dto;

import lombok.Data;

/**
 * 管理端角色简要信息DTO，用于全量角色列表
 *
 * @author lesofn
 */
@Data
public class AdminRoleSimpleDTO {

    /** 角色ID */
    private Long id;

    /** 角色名称 */
    private String name;

    public static AdminRoleSimpleDTO of(Long id, String name) {
        AdminRoleSimpleDTO dto = new AdminRoleSimpleDTO();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }
}
