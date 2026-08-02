package com.lesofn.archforge.user.infrastructure.adapter.repository;

import com.lesofn.archforge.user.domain.adapter.repository.UserRepository;
import com.lesofn.archforge.user.domain.model.aggregate.UserAggregate;
import com.lesofn.archforge.user.domain.valueobject.UserId;
import com.lesofn.archforge.user.infrastructure.adapter.repository.po.UserPO;
import com.lesofn.archforge.user.infrastructure.dao.UserDao;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 用户聚合仓储实现。
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserDao userDao;
    private final UserPOConvertor convertor;

    public UserRepositoryImpl(UserDao userDao, UserPOConvertor convertor) {
        this.userDao = userDao;
        this.convertor = convertor;
    }

    @Override
    public Optional<UserAggregate> findById(UserId id) {
        if (id == null) {
            return Optional.empty();
        }
        return this.userDao.findById(id.value()).map(this.convertor::toAggregate);
    }

    @Override
    public UserAggregate save(UserAggregate aggregate) {
        if (aggregate == null) {
            throw new IllegalArgumentException("User aggregate must not be null");
        }
        UserPO po = this.convertor.toPo(aggregate);

        // 若已存在，加载旧 PO 并合并未在领域模型中维护的字段，避免数据丢失
        if (po.getId() != null) {
            Optional<UserPO> existing = this.userDao.findById(po.getId());
            if (existing.isPresent()) {
                mergePreservedFields(po, existing.get());
            }
        }

        UserPO saved = this.userDao.save(po);
        return this.convertor.toAggregate(saved);
    }

    @Override
    public void deleteById(UserId id) {
        if (id == null) {
            throw new IllegalArgumentException("User id must not be null");
        }
        this.userDao.deleteById(id.value());
    }

    private void mergePreservedFields(UserPO target, UserPO source) {
        if (target.getNickname() == null || target.getNickname().isBlank()) {
            target.setNickname(source.getNickname());
        }
        if (target.getUserType() == null) {
            target.setUserType(source.getUserType());
        }
        if (target.getSex() == null) {
            target.setSex(source.getSex());
        }
        if (target.getAvatar() == null || target.getAvatar().isBlank()) {
            target.setAvatar(source.getAvatar());
        }
        if (target.getLoginIp() == null || target.getLoginIp().isBlank()) {
            target.setLoginIp(source.getLoginIp());
        }
        if (target.getLoginDate() == null) {
            target.setLoginDate(source.getLoginDate());
        }
        if (target.getIsAdmin() == null) {
            target.setIsAdmin(source.getIsAdmin());
        }
        if (target.getRemark() == null || target.getRemark().isBlank()) {
            target.setRemark(source.getRemark());
        }
        if (target.getDeptId() == null) {
            target.setDeptId(source.getDeptId());
        }
        // 审计字段和逻辑删除字段由 BasePO / 数据库维护
        target.setCreateTime(source.getCreateTime());
        target.setCreatorId(source.getCreatorId());
        target.setDeleted(source.getDeleted());
    }
}
