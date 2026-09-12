package com.lesofn.archforge.common.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.jspecify.annotations.Nullable;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@SuppressWarnings("NullAway.Init") // fields populated via setters/builders
public class SystemLoginUser extends BaseLoginUser {

    private boolean isAdmin;

    private @Nullable Long deptId;

    private @Nullable RoleInfo roleInfo;

    /** 当超过这个时间 则触发刷新缓存时间 */
    private Long autoRefreshCacheTime;

    @JsonCreator
    public SystemLoginUser(
            @JsonProperty("userId") Long userId,
            @JsonProperty("admin") Boolean isAdmin,
            @JsonProperty("username") String username,
            @JsonProperty("password") @Nullable String password,
            @JsonProperty("roleInfo") @Nullable RoleInfo roleInfo,
            @JsonProperty("deptId") @Nullable Long deptId) {
        this.userId = userId;
        this.isAdmin = isAdmin != null ? isAdmin : false;
        this.username = username;
        this.password = password;
        this.roleInfo = roleInfo;
        this.deptId = deptId;
    }

    public Long getRoleId() { return Optional.ofNullable(getRoleInfo()).map(RoleInfo::getRoleId).orElse(0L); }
}
