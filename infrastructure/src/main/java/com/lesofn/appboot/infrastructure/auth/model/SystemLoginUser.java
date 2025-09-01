package com.lesofn.appboot.infrastructure.auth.model;

import com.lesofn.appboot.infrastructure.user.base.BaseLoginUser;
import com.lesofn.appboot.infrastructure.user.web.RoleInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 登录用户身份权限
 * @author sofn
 */
@Setter
@Getter
@NoArgsConstructor
public class SystemLoginUser extends BaseLoginUser {

    private static final long serialVersionUID = 1L;

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

    public RoleInfo getRoleInfo() {
        return roleInfo;
    }

    public Long getRoleId() {
        return getRoleInfo().getRoleId();
    }

    public Long getDeptId() {
        return deptId;
    }


}
