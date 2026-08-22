package com.lesofn.archforge.user.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.common.enums.common.GenderEnum;
import com.lesofn.archforge.user.api.domain.SysRole;
import com.lesofn.archforge.user.api.domain.SysUser;
import org.junit.jupiter.api.Test;

/** Verifies the default-valid contract of the user/role test data builders. */
class UserAndRoleBuilderTest {

    @Test
    void buildsActiveUserByDefault() {
        SysUser user = UserTestBuilder.aUser().build();

        assertTrue(user.canLogin());
        assertEquals(Boolean.FALSE, user.getIsAdmin());
        assertNotNull(user.getUsername(), "username must be set for unique constraints");
        assertNotNull(user.getPassword());
    }

    @Test
    void overridesAreRespected() {
        SysUser user = UserTestBuilder.aUser()
                .withUsername("alice")
                .asAdmin()
                .withSex(GenderEnum.FEMALE)
                .disabled()
                .build();

        assertEquals("alice", user.getUsername());
        assertTrue(user.getIsAdmin());
        assertEquals(GenderEnum.FEMALE, user.getSex());
        assertFalse(user.canLogin());
    }

    @Test
    void generatesUniqueUsernamesAcrossBuilds() {
        String first = UserTestBuilder.aUser().build().getUsername();
        String second = UserTestBuilder.aUser().build().getUsername();

        assertNotEquals(first, second);
    }

    @Test
    void buildsEnabledRoleByDefault() {
        SysRole role = RoleTestBuilder.aRole().build();

        assertTrue(role.isEnabled());
        assertNotNull(role.getRoleKey());
    }

    @Test
    void disabledRoleIsNotEnabled() {
        SysRole role = RoleTestBuilder.aRole().disabled().build();

        assertFalse(role.isEnabled());
    }
}
