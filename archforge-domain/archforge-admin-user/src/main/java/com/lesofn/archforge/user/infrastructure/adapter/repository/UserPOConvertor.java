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
                Email.ofNullable(po.getEmail()),
                PhoneNumber.ofNullable(po.getPhoneNumber()),
                Password.ofEncrypted(po.getPassword()),
                UserStatus.fromPersistenceValue(po.getStatus()));

        RoleId roleId = new RoleId(po.getRoleId() == null || po.getRoleId() < 0L ? 0L : po.getRoleId());
        UserAggregate aggregate = new UserAggregate(user, roleId, po.getDeptId(), po.getNickname(), po.getUserType(), po
                .getSex(), po.getAvatar(), po.getLoginIp(), po.getLoginDate(), po.getIsAdmin(), po.getRemark(), Boolean.TRUE
                        .equals(po.getDeleted()));
        aggregate.replaceAudit(po.getCreatorId(), po.getCreateTime(), po.getUpdaterId(), po.getUpdateTime());
        return aggregate;
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
        po.setId(user.getId() == null ? null : user.getId().value());
        po.setRoleId(aggregate.getRoleId() == null ? null : aggregate.getRoleId().value());
        po.setDeptId(aggregate.getDeptId());
        po.setUsername(user.getUsername().value());
        po.setNickname(aggregate.getNickname());
        po.setUserType(aggregate.getUserType());
        po.setEmail(user.getEmail().value());
        po.setPhoneNumber(user.getPhoneNumber().value());
        po.setSex(aggregate.getSex());
        po.setAvatar(aggregate.getAvatar());
        po.setPassword(user.getPassword().value());
        po.setStatus(user.getStatus().toPersistenceValue());
        po.setLoginIp(aggregate.getLoginIp());
        po.setLoginDate(aggregate.getLoginDate());
        po.setIsAdmin(aggregate.getIsAdmin());
        po.setRemark(aggregate.getRemark());
        po.setDeleted(aggregate.isDeleted());
        po.setCreatorId(aggregate.getCreatorId());
        po.setCreateTime(aggregate.getCreateTime());
        po.setUpdaterId(aggregate.getUpdaterId());
        po.setUpdateTime(aggregate.getUpdateTime());
        return po;
    }
}
