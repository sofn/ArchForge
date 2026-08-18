package com.lesofn.archforge.infrastructure.auth.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lesofn.archforge.infrastructure.user.base.BaseLoginUser;
import com.lesofn.archforge.infrastructure.user.web.RoleInfo;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 登录用户身份权限
 *
 * @author sofn
 */
@Setter
@Getter
@NoArgsConstructor
public class SystemLoginUser extends BaseLoginUser {

    private boolean isAdmin;

    private Long deptId;

    private RoleInfo roleInfo;

    /** 当超过这个时间 则触发刷新缓存时间 */
    private Long autoRefreshCacheTime;

    @JsonCreator
    public SystemLoginUser(
            @JsonProperty("userId") Long userId,
            @JsonProperty("admin") Boolean isAdmin,
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("roleInfo") RoleInfo roleInfo,
            @JsonProperty("deptId") Long deptId) {
        this.userId = userId;
        this.isAdmin = isAdmin != null ? isAdmin : false;
        this.username = username;
        this.password = password;
        this.roleInfo = roleInfo;
        this.deptId = deptId;
    }

    public Long getRoleId() { return Optional.ofNullable(getRoleInfo()).map(RoleInfo::getRoleId).orElse(0L); }
}
