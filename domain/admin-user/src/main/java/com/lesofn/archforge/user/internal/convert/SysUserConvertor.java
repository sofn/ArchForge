package com.lesofn.archforge.user.internal.convert;

import com.lesofn.archforge.common.enums.common.GenderEnum;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.domain.model.aggregate.UserAggregate;
import com.lesofn.archforge.user.domain.model.entity.User;
import com.lesofn.archforge.user.domain.valueobject.Email;
import com.lesofn.archforge.user.domain.valueobject.Password;
import com.lesofn.archforge.user.domain.valueobject.PhoneNumber;
import com.lesofn.archforge.user.domain.valueobject.RoleId;
import com.lesofn.archforge.user.domain.valueobject.UserId;
import com.lesofn.archforge.user.domain.valueobject.UserStatus;
import com.lesofn.archforge.user.domain.valueobject.Username;
import org.springframework.stereotype.Component;

@Component
public class SysUserConvertor {

    public SysUser toSysUser(UserAggregate aggregate) {
        if (aggregate == null) {
            return null;
        }
        User user = aggregate.getUser();
        SysUser sysUser = new SysUser();
        if (user.getId() != null) {
            sysUser.setUserId(user.getId().value());
        }
        sysUser.setRoleId(aggregate.getRoleId() == null ? null : zeroToNull(aggregate.getRoleId().value()));
        sysUser.setDeptId(aggregate.getDeptId());
        sysUser.setUsername(user.getUsername().value());
        sysUser.setNickname(aggregate.getNickname());
        sysUser.setUserType(aggregate.getUserType());
        sysUser.setEmail(user.getEmail().value());
        sysUser.setPhoneNumber(user.getPhoneNumber().value());
        sysUser.setSex(aggregate.getSex() == null ? null : GenderEnum.fromValue(aggregate.getSex()));
        sysUser.setAvatar(aggregate.getAvatar());
        sysUser.setPassword(user.getPassword().value());
        sysUser.setStatus(user.getStatus().toPersistenceValue());
        sysUser.setLoginIp(aggregate.getLoginIp());
        sysUser.setLoginDate(aggregate.getLoginDate());
        sysUser.setIsAdmin(aggregate.getIsAdmin());
        sysUser.setRemark(aggregate.getRemark());
        sysUser.setDeleted(aggregate.isDeleted());
        return sysUser;
    }

    public UserAggregate toAggregate(SysUser sysUser) {
        if (sysUser == null) {
            return null;
        }
        UserId userId = sysUser.getUserId() == null ? null : new UserId(sysUser.getUserId());
        User user = User.create(
                userId,
                new Username(sysUser.getUsername()),
                Email.ofNullable(sysUser.getEmail()),
                PhoneNumber.ofNullable(sysUser.getPhoneNumber()),
                Password.ofEncrypted(sysUser.getPassword()),
                UserStatus.fromPersistenceValue(sysUser.getStatus()));
        RoleId roleId = new RoleId(sysUser.getRoleId() == null ? 0L : sysUser.getRoleId());
        return new UserAggregate(user, roleId, sysUser.getDeptId(), sysUser.getNickname(), sysUser.getUserType(), sysUser
                .getSex() == null ? null : sysUser.getSex().getValue(), sysUser.getAvatar(), sysUser.getLoginIp(), sysUser
                        .getLoginDate(), sysUser.getIsAdmin(), sysUser.getRemark(), Boolean.TRUE.equals(sysUser.getDeleted()));
    }

    private static Long zeroToNull(Long value) {
        return value == null || value == 0L ? null : value;
    }
}
