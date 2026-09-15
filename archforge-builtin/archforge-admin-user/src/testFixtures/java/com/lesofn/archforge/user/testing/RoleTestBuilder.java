package com.lesofn.archforge.user.testing;

import com.lesofn.archforge.user.api.domain.SysRole;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test data builder for {@link SysRole}. Produces valid-by-default enabled roles.
 */
public final class RoleTestBuilder {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private final SysRole role = new SysRole();

    private RoleTestBuilder() {
    }

    public static RoleTestBuilder aRole() {
        return new RoleTestBuilder();
    }

    public RoleTestBuilder withRoleId(Long roleId) {
        role.setRoleId(roleId);
        return this;
    }

    public RoleTestBuilder withRoleName(String roleName) {
        role.setRoleName(roleName);
        return this;
    }

    public RoleTestBuilder withRoleKey(String roleKey) {
        role.setRoleKey(roleKey);
        return this;
    }

    public RoleTestBuilder withRoleSort(Integer roleSort) {
        role.setRoleSort(roleSort);
        return this;
    }

    public RoleTestBuilder withDataScope(Short dataScope) {
        role.setDataScope(dataScope);
        return this;
    }

    public RoleTestBuilder withStatus(Short status) {
        role.setStatus(status);
        return this;
    }

    public RoleTestBuilder disabled() {
        role.setStatus((short) 0);
        return this;
    }

    public RoleTestBuilder deleted() {
        role.setDeleted(true);
        return this;
    }

    public SysRole build() {
        long seq = SEQUENCE.incrementAndGet();
        if (role.getRoleName() == null) {
            role.setRoleName("角色" + seq);
        }
        if (role.getRoleKey() == null) {
            role.setRoleKey("ROLE_" + seq);
        }
        if (role.getRoleSort() == null) {
            role.setRoleSort((int) seq);
        }
        if (role.getStatus() == null) {
            role.setStatus((short) 1);
        }
        if (role.getDataScope() == null) {
            role.setDataScope((short) 1);
        }
        if (role.getDeleted() == null) {
            role.setDeleted(false);
        }
        return role;
    }
}
