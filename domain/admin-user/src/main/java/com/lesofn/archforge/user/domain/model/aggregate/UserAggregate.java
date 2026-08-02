package com.lesofn.archforge.user.domain.model.aggregate;

import com.lesofn.archforge.common.domain.BaseDomainEntity;
import com.lesofn.archforge.user.domain.event.RoleAssignedEvent;
import com.lesofn.archforge.user.domain.event.UserCreatedEvent;
import com.lesofn.archforge.user.domain.event.UserDisabledEvent;
import com.lesofn.archforge.user.domain.event.UserEnabledEvent;
import com.lesofn.archforge.user.domain.event.UserPasswordChangedEvent;
import com.lesofn.archforge.user.domain.model.entity.User;
import com.lesofn.archforge.user.domain.valueobject.Email;
import com.lesofn.archforge.user.domain.valueobject.Password;
import com.lesofn.archforge.user.domain.valueobject.PhoneNumber;
import com.lesofn.archforge.user.domain.valueobject.RoleId;
import com.lesofn.archforge.user.domain.valueobject.UserId;
import com.lesofn.archforge.user.domain.valueobject.Username;
import java.io.Serial;
import java.util.Objects;
import lombok.Getter;

/**
 * 用户聚合根。
 *
 * <p>
 * 包含 {@link User} 实体与角色引用，负责维护聚合内不变量并发布领域事件。
 */
@Getter
public class UserAggregate extends BaseDomainEntity<UserId> {

    @Serial
    private static final long serialVersionUID = 1L;

    private final User user;
    private RoleId roleId;

    public UserAggregate(User user, RoleId roleId) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        if (roleId == null) {
            throw new IllegalArgumentException("Role id must not be null");
        }
        this.user = user;
        this.roleId = roleId;
        setId(user.getId());
    }

    /**
     * 创建新的用户聚合。
     */
    public static UserAggregate create(
            UserId id,
            Username username,
            Email email,
            PhoneNumber phoneNumber,
            Password password,
            RoleId roleId) {
        User user = User.create(id, username, email, phoneNumber, password);
        UserAggregate aggregate = new UserAggregate(user, roleId);
        aggregate.registerEvent(new UserCreatedEvent(id));
        return aggregate;
    }

    /**
     * 禁用用户。
     */
    public void disable() {
        this.user.disable();
        registerEvent(new UserDisabledEvent(getId()));
    }

    /**
     * 启用用户。
     */
    public void enable() {
        this.user.enable();
        registerEvent(new UserEnabledEvent(getId()));
    }

    /**
     * 修改用户密码。
     */
    public void changePassword(Password newPassword) {
        this.user.changePassword(newPassword);
        registerEvent(new UserPasswordChangedEvent(getId()));
    }

    /**
     * 分配角色。
     */
    public void assignRole(RoleId roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("Role id must not be null");
        }
        if (Objects.equals(this.roleId, roleId)) {
            throw new IllegalStateException("User already has role " + roleId.value());
        }
        this.roleId = roleId;
        registerEvent(new RoleAssignedEvent(getId(), roleId));
    }

    /**
     * 用户当前是否可以登录。
     */
    public boolean canLogin() {
        return this.user.isActive();
    }
}
