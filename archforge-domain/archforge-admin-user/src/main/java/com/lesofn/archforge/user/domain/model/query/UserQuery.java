package com.lesofn.archforge.user.domain.model.query;

import lombok.Data;

/**
 * 用户领域查询条件。
 */
@Data
public class UserQuery {

    private String username;
    private String email;
    private String phoneNumber;
    private Boolean enabled;
}
