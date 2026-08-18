package com.lesofn.archforge.user.domain.service;

import com.lesofn.archforge.user.domain.model.aggregate.UserAggregate;
import com.lesofn.archforge.user.domain.valueobject.Email;
import com.lesofn.archforge.user.domain.valueobject.Password;
import com.lesofn.archforge.user.domain.valueobject.PhoneNumber;
import com.lesofn.archforge.user.domain.valueobject.RoleId;
import com.lesofn.archforge.user.domain.valueobject.UserId;
import com.lesofn.archforge.user.domain.valueobject.Username;

/**
 * 用户领域服务。
 */
public interface UserDomainService {

    UserAggregate createUser(
            UserId id,
            Username username,
            Email email,
            PhoneNumber phoneNumber,
            Password password,
            RoleId roleId);

    void disableUser(UserId userId);

    void enableUser(UserId userId);

    void changePassword(UserId userId, Password newPassword);

    void assignRole(UserId userId, RoleId roleId);
}
