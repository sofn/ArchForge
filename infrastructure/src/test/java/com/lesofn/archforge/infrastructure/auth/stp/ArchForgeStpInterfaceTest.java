package com.lesofn.archforge.infrastructure.auth.stp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cn.dev33.satoken.session.SaSession;
import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.user.web.DataScopeEnum;
import com.lesofn.archforge.infrastructure.user.web.RoleInfo;
import java.util.List;
import java.util.Set;
import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArchForgeStpInterfaceTest {

    private final ArchForgeStpInterface stpInterface = new ArchForgeStpInterface();

    @BeforeEach
    void setUp() {
        SaTokenContextMockUtil.setMockContext();
    }

    @AfterEach
    void tearDown() {
        StpAdminUtil.logout();
        SaTokenContextMockUtil.clearContext();
    }

    @Test
    void adminLoginReturnsAdminRoleAndMenuPermissions() {
        RoleInfo roleInfo = new RoleInfo(RoleInfo.ADMIN_ROLE_ID, RoleInfo.ADMIN_ROLE_KEY, DataScopeEnum.ALL, Set.of(), Set.of(
                "user:list", "user:add"), Set.of(1L));
        SystemLoginUser loginUser = new SystemLoginUser(1L, true, "admin", "secret", roleInfo, 4L);

        SaSession session = StpAdminUtil.getSessionByLoginId(1L, true);
        session.set(LoginSessionKeys.LOGIN_USER, loginUser);

        List<String> roles = stpInterface.getRoleList(1L, StpAdminUtil.TYPE);
        List<String> permissions = stpInterface.getPermissionList(1L, StpAdminUtil.TYPE);

        assertEquals(List.of("ADMIN"), roles);
        assertTrue(permissions.contains("user:list"));
        assertTrue(permissions.contains("user:add"));
    }

    @Test
    void adminLoginReturnsRoleKeyForNormalUser() {
        RoleInfo roleInfo = new RoleInfo(2L, "editor", DataScopeEnum.ONLY_SELF, Set.of(), Set.of("blog:list"), Set.of(2L));
        SystemLoginUser loginUser = new SystemLoginUser(8L, false, "alice", "secret", roleInfo, 5L);
        StpAdminUtil.getSessionByLoginId(8L, true).set(LoginSessionKeys.LOGIN_USER, loginUser);

        List<String> roles = stpInterface.getRoleList(8L, StpAdminUtil.TYPE);
        List<String> permissions = stpInterface.getPermissionList(8L, StpAdminUtil.TYPE);

        assertEquals(List.of("editor"), roles);
        assertEquals(List.of("blog:list"), permissions);
    }

    @Test
    void webLoginTypeHasNoRolesOrPermissions() {
        assertTrue(stpInterface.getRoleList(3L, StpWebUtil.TYPE).isEmpty());
        assertTrue(stpInterface.getPermissionList(3L, StpWebUtil.TYPE).isEmpty());
    }
}
