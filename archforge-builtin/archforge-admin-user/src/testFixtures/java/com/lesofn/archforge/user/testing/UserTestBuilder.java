package com.lesofn.archforge.user.testing;

import com.lesofn.archforge.common.enums.common.GenderEnum;
import com.lesofn.archforge.common.enums.common.UserStatusEnum;
import com.lesofn.archforge.user.api.domain.SysUser;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test data builder for {@link SysUser}. Produces valid-by-default users so tests only
 * configure the fields they care about.
 */
public final class UserTestBuilder {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private final SysUser user = new SysUser();

    private UserTestBuilder() {
    }

    public static UserTestBuilder aUser() {
        return new UserTestBuilder();
    }

    public UserTestBuilder withUserId(Long userId) {
        user.setUserId(userId);
        return this;
    }

    public UserTestBuilder withUsername(String username) {
        user.setUsername(username);
        return this;
    }

    public UserTestBuilder withNickname(String nickname) {
        user.setNickname(nickname);
        return this;
    }

    /** The value must already be encoded; builders never encode passwords themselves. */
    public UserTestBuilder withEncodedPassword(String encodedPassword) {
        user.setPassword(encodedPassword);
        return this;
    }

    public UserTestBuilder withEmail(String email) {
        user.setEmail(email);
        return this;
    }

    public UserTestBuilder withPhoneNumber(String phoneNumber) {
        user.setPhoneNumber(phoneNumber);
        return this;
    }

    public UserTestBuilder withSex(GenderEnum sex) {
        user.setSex(sex);
        return this;
    }

    public UserTestBuilder withRoleId(Long roleId) {
        user.setRoleId(roleId);
        return this;
    }

    public UserTestBuilder withDeptId(Long deptId) {
        user.setDeptId(deptId);
        return this;
    }

    public UserTestBuilder withStatus(Integer status) {
        user.setStatus(status);
        return this;
    }

    public UserTestBuilder asAdmin() {
        user.setIsAdmin(true);
        return this;
    }

    public UserTestBuilder disabled() {
        user.setStatus(UserStatusEnum.DISABLED.getValue());
        return this;
    }

    public UserTestBuilder deleted() {
        user.setDeleted(true);
        return this;
    }

    public SysUser build() {
        long seq = SEQUENCE.incrementAndGet();
        if (user.getUsername() == null) {
            user.setUsername("user-" + seq);
        }
        if (user.getNickname() == null) {
            user.setNickname("用户" + seq);
        }
        if (user.getPassword() == null) {
            user.setPassword("{noop}password-" + seq);
        }
        if (user.getStatus() == null) {
            user.setStatus(UserStatusEnum.NORMAL.getValue());
        }
        if (user.getSex() == null) {
            user.setSex(GenderEnum.MALE);
        }
        if (user.getIsAdmin() == null) {
            user.setIsAdmin(false);
        }
        if (user.getDeleted() == null) {
            user.setDeleted(false);
        }
        return user;
    }
}
