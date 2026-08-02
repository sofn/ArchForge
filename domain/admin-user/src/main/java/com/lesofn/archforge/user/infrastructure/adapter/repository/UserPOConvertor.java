package com.lesofn.archforge.user.infrastructure.adapter.repository;

import com.lesofn.archforge.user.domain.model.aggregate.UserAggregate;
import com.lesofn.archforge.user.domain.model.entity.User;
import com.lesofn.archforge.user.domain.valueobject.Email;
import com.lesofn.archforge.user.domain.valueobject.Password;
import com.lesofn.archforge.user.domain.valueobject.PhoneNumber;
import com.lesofn.archforge.user.domain.valueobject.RoleId;
import com.lesofn.archforge.user.domain.valueobject.UserId;
import com.lesofn.archforge.user.domain.valueobject.UserStatus;
import com.lesofn.archforge.user.domain.valueobject.Username;
import com.lesofn.archforge.user.infrastructure.adapter.repository.po.UserPO;
import org.mapstruct.Mapper;

/**
 * UserPO 与 UserAggregate 之间的 MapStruct 转换器。
 *
 * <p>
 * 由于 {@link User} 与 {@link UserAggregate} 通过工厂方法构造，核心转换逻辑以 default 方法实现。
 */
@Mapper(componentModel = "spring")
public interface UserPOConvertor {

    /**
     * 将持久化对象转换为领域聚合。
     */
    default UserAggregate toAggregate(UserPO po) {
        if (po == null) {
            return null;
        }
        User user = User.create(
                new UserId(po.getId()),
                new Username(po.getUsername()),
                new Email(po.getEmail() == null ? "" : po.getEmail()),
                new PhoneNumber(po.getPhoneNumber() == null ? "" : po.getPhoneNumber()),
                Password.ofEncrypted(po.getPassword()),
                toUserStatus(po.getStatus()));

        RoleId roleId = po.getRoleId() == null ? new RoleId(0L) : new RoleId(po.getRoleId());
        return new UserAggregate(user, roleId);
    }

    /**
     * 将领域聚合转换为持久化对象。
     */
    default UserPO toPo(UserAggregate aggregate) {
        if (aggregate == null) {
            return null;
        }
        User user = aggregate.getUser();
        UserPO po = new UserPO();
        po.setId(user.getId().value());
        po.setRoleId(aggregate.getRoleId().value());
        po.setUsername(user.getUsername().value());
        po.setEmail(user.getEmail().value());
        po.setPhoneNumber(user.getPhoneNumber().value());
        po.setPassword(user.getPassword().value());
        po.setStatus(toStatusValue(user.getStatus()));

        // 领域模型未包含的字段使用默认值，避免覆盖已有数据
        po.setNickname("");
        po.setUserType(0);
        po.setSex(0);
        po.setAvatar("");
        po.setIsAdmin(Boolean.FALSE);
        po.setRemark("");
        return po;
    }

    /**
     * 将领域状态转换为数据库存储值。
     */
    default Integer toStatusValue(UserStatus status) {
        if (status == null) {
            return UserStatus.NORMAL.ordinal() + 1;
        }
        return status.ordinal() + 1;
    }

    /**
     * 将数据库存储值转换为领域状态。
     */
    default UserStatus toUserStatus(Integer value) {
        if (value == null) {
            return UserStatus.NORMAL;
        }
        return switch (value) {
            case 2 -> UserStatus.DISABLED;
            case 3 -> UserStatus.FROZEN;
            default -> UserStatus.NORMAL;
        };
    }
}
