package com.lesofn.archforge.user.domain.model.entity;

import com.lesofn.archforge.user.domain.valueobject.Email;
import com.lesofn.archforge.user.domain.valueobject.Password;
import com.lesofn.archforge.user.domain.valueobject.PhoneNumber;
import com.lesofn.archforge.user.domain.valueobject.UserId;
import com.lesofn.archforge.user.domain.valueobject.UserStatus;
import com.lesofn.archforge.user.domain.valueobject.Username;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * 用户领域实体。
 *
 * <p>
 * 纯 Java 对象，无 JPA 等框架注解；所有状态变更通过业务方法完成，并内置不变量保护。
 */
@Getter
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final @Nullable UserId id;
    private Username username;
    private Email email;
    private PhoneNumber phoneNumber;
    private Password password;
    private UserStatus status;

    private User(
            @Nullable UserId id,
            Username username,
            Email email,
            PhoneNumber phoneNumber,
            Password password,
            UserStatus status) {
        if (username == null) {
            throw new IllegalArgumentException("Username must not be null");
        }
        if (email == null) {
            throw new IllegalArgumentException("Email must not be null");
        }
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number must not be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null");
        }
        this.id = id;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.status = status;
    }

    /**
     * 创建新用户，初始状态为 {@link UserStatus#NORMAL}。
     */
    public static User create(
            UserId id,
            Username username,
            Email email,
            PhoneNumber phoneNumber,
            Password password) {
        return create(id, username, email, phoneNumber, password, UserStatus.NORMAL);
    }

    /**
     * 创建用户，指定初始状态。
     */
    public static User create(
            UserId id,
            Username username,
            Email email,
            PhoneNumber phoneNumber,
            Password password,
            UserStatus status) {
        return new User(id, username, email, phoneNumber, password, status);
    }

    /**
     * 禁用账户。
     */
    public void disable() {
        if (!this.status.canTransitionTo(UserStatus.DISABLED)) {
            throw new IllegalStateException("Cannot disable user with status " + this.status);
        }
        this.status = UserStatus.DISABLED;
    }

    /**
     * 启用账户。
     */
    public void enable() {
        if (!this.status.canTransitionTo(UserStatus.NORMAL)) {
            throw new IllegalStateException("Cannot enable user with status " + this.status);
        }
        this.status = UserStatus.NORMAL;
    }

    /**
     * 修改密码。
     *
     * @param newPassword 新的加密后密码
     */
    public void changePassword(Password newPassword) {
        if (newPassword == null) {
            throw new IllegalArgumentException("New password must not be null");
        }
        this.password = newPassword;
    }

    public void rename(Username username) {
        if (username == null) {
            throw new IllegalArgumentException("Username must not be null");
        }
        this.username = username;
    }

    public void changeEmail(Email email) {
        if (email == null) {
            throw new IllegalArgumentException("Email must not be null");
        }
        this.email = email;
    }

    public void changePhoneNumber(PhoneNumber phoneNumber) {
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number must not be null");
        }
        this.phoneNumber = phoneNumber;
    }

    public void updateStatus(UserStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null");
        }
        if (this.status != status && !this.status.canTransitionTo(status)) {
            throw new IllegalStateException("Cannot change user status from " + this.status + " to " + status);
        }
        this.status = status;
    }

    /**
     * 用户是否处于正常可用状态。
     */
    public boolean isActive() { return this.status == UserStatus.NORMAL; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(this.id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }
}
