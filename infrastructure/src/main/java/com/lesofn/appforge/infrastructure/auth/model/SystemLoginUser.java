package com.lesofn.appforge.infrastructure.auth.model;

import com.lesofn.appforge.infrastructure.user.base.BaseLoginUser;
import com.lesofn.appforge.infrastructure.user.web.RoleInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

/**
 * 登录用户身份权限
 * @author sofn
 */
@Setter
@Getter
@NoArgsConstructor
public class SystemLoginUser extends BaseLoginUser {

    private boolean isAdmin;

    private Long deptId;

    private RoleInfo roleInfo;

    /**
     * 当超过这个时间 则触发刷新缓存时间
     */
    private Long autoRefreshCacheTime;


    public SystemLoginUser(Long userId, Boolean isAdmin, String username, String password, RoleInfo roleInfo,
                           Long deptId) {
        this.userId = userId;
        this.isAdmin = isAdmin;
        this.username = username;
        this.password = password;
        this.roleInfo = roleInfo;
        this.deptId = deptId;
    }

    public Long getRoleId() {
        return Optional.ofNullable(getRoleInfo()).map(RoleInfo::getRoleId).orElse(0L);
    }

}
