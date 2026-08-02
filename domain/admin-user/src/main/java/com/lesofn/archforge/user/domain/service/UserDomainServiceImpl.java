package com.lesofn.archforge.user.domain.service;

import com.lesofn.archforge.user.domain.model.aggregate.UserAggregate;
import com.lesofn.archforge.user.domain.adapter.repository.UserRepository;
import com.lesofn.archforge.user.domain.valueobject.Email;
import com.lesofn.archforge.user.domain.valueobject.Password;
import com.lesofn.archforge.user.domain.valueobject.PhoneNumber;
import com.lesofn.archforge.user.domain.valueobject.RoleId;
import com.lesofn.archforge.user.domain.valueobject.UserId;
import com.lesofn.archforge.user.domain.valueobject.Username;
import java.util.Objects;

/**
 * 用户领域服务实现。
 *
 * <p>
 * 纯 Java 实现，通过构造注入 {@link UserRepository}。
 */
public class UserDomainServiceImpl implements UserDomainService {

    private final UserRepository userRepository;

    public UserDomainServiceImpl(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "User repository must not be null");
    }

    @Override
    public UserAggregate createUser(
            UserId id,
            Username username,
            Email email,
            PhoneNumber phoneNumber,
            Password password,
            RoleId roleId) {
        UserAggregate aggregate = UserAggregate.create(id, username, email, phoneNumber, password, roleId);
        return this.userRepository.save(aggregate);
    }

    @Override
    public void disableUser(UserId userId) {
        UserAggregate aggregate = find(userId);
        aggregate.disable();
        this.userRepository.save(aggregate);
    }

    @Override
    public void enableUser(UserId userId) {
        UserAggregate aggregate = find(userId);
        aggregate.enable();
        this.userRepository.save(aggregate);
    }

    @Override
    public void changePassword(UserId userId, Password newPassword) {
        UserAggregate aggregate = find(userId);
        aggregate.changePassword(newPassword);
        this.userRepository.save(aggregate);
    }

    @Override
    public void assignRole(UserId userId, RoleId roleId) {
        UserAggregate aggregate = find(userId);
        aggregate.assignRole(roleId);
        this.userRepository.save(aggregate);
    }

    private UserAggregate find(UserId userId) {
        return this.userRepository
                .findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId.value()));
    }
}
