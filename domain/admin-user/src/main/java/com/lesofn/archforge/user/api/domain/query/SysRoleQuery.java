package com.lesofn.archforge.user.api.domain.query;

import lombok.Data;

@Data
public class SysRoleQuery {
    private String roleName;
    private String roleKey;
    private Short status;
}
