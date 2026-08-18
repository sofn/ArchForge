package com.lesofn.archforge.infrastructure.auth.stp;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.user.web.RoleInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ArchForgeStpInterface implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        if (!StpAdminUtil.TYPE.equals(loginType)) {
            return Collections.emptyList();
        }
        SystemLoginUser loginUser = findAdminUser(loginId);
        if (loginUser == null || loginUser.getRoleInfo() == null || loginUser.getRoleInfo().getMenuPermissions() == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(loginUser.getRoleInfo().getMenuPermissions());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        if (!StpAdminUtil.TYPE.equals(loginType)) {
            return Collections.emptyList();
        }
        SystemLoginUser loginUser = findAdminUser(loginId);
        if (loginUser == null) {
            return Collections.emptyList();
        }
        if (loginUser.isAdmin()) {
            return List.of("ADMIN", RoleInfo.ADMIN_ROLE_KEY);
        }
        RoleInfo roleInfo = loginUser.getRoleInfo();
        if (roleInfo == null || roleInfo.getRoleKey() == null || roleInfo.getRoleKey().isBlank()) {
            return Collections.emptyList();
        }
        return List.of(roleInfo.getRoleKey());
    }

    private static SystemLoginUser findAdminUser(Object loginId) {
        SaSession session = StpAdminUtil.getSessionByLoginId(loginId, false);
        if (session == null) {
            return null;
        }
        Object value = session.get(LoginSessionKeys.LOGIN_USER);
        if (value instanceof SystemLoginUser loginUser) {
            return loginUser;
        }
        return null;
    }
}
