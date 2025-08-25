package com.lesofn.matrixboot.user.domain.query;

import lombok.Data;

/**
 * @author lesofn
 */
@Data
public class SysUserQuery {
    private String username;
    private String email;
    private String phoneNumber;
    private Boolean enabled;
}