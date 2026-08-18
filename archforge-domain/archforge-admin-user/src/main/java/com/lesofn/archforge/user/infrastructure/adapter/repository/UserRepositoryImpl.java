package com.lesofn.archforge.user.infrastructure.adapter.repository;

import com.lesofn.archforge.user.domain.adapter.repository.UserRepository;
import com.lesofn.archforge.user.domain.model.aggregate.UserAggregate;
import com.lesofn.archforge.user.domain.model.query.UserQuery;
import com.lesofn.archforge.user.domain.valueobject.Email;
import com.lesofn.archforge.user.domain.valueobject.PhoneNumber;
import com.lesofn.archforge.user.domain.valueobject.UserId;
import com.lesofn.archforge.user.domain.valueobject.UserStatus;
import com.lesofn.archforge.user.domain.valueobject.Username;
import com.lesofn.archforge.user.infrastructure.adapter.repository.po.UserPO;
import com.lesofn.archforge.user.infrastructure.dao.UserDao;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

/**
 * 用户聚合仓储实现。
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private static final char LIKE_ESCAPE_CHAR = '!';

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
    public Optional<UserAggregate> findByUsername(Username username) {
        if (username == null) {
            return Optional.empty();
        }
        return this.userDao.findByUsername(username.value()).map(this.convertor::toAggregate);
    }

    @Override
    public Optional<UserAggregate> findByEmail(Email email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return this.userDao.findByEmail(email.value()).map(this.convertor::toAggregate);
    }

    @Override
    public Optional<UserAggregate> findByPhoneNumber(PhoneNumber phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return Optional.empty();
        }
        return this.userDao.findByPhoneNumber(phoneNumber.value()).map(this.convertor::toAggregate);
    }

    @Override
    public UserAggregate save(UserAggregate aggregate) {
        if (aggregate == null) {
            throw new IllegalArgumentException("User aggregate must not be null");
        }
        UserPO po = this.convertor.toPo(aggregate);
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

    @Override
    public Page<UserAggregate> findAll(Pageable pageable) {
        return this.userDao.findAll(pageable).map(this.convertor::toAggregate);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<UserAggregate> findAll(Specification<?> spec, Pageable pageable) {
        Specification<UserPO> userSpec = spec == null ? null : (Specification<UserPO>) spec;
        return this.userDao.findAll(userSpec, pageable).map(this.convertor::toAggregate);
    }

    @Override
    public Page<UserAggregate> search(UserQuery query, Pageable pageable) {
        Specification<UserPO> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null) {
                if (isNotBlank(query.getUsername())) {
                    predicates.add(like(cb, root, "username", query.getUsername()));
                }
                if (isNotBlank(query.getEmail())) {
                    predicates.add(like(cb, root, "email", query.getEmail()));
                }
                if (isNotBlank(query.getPhoneNumber())) {
                    predicates.add(like(cb, root, "phoneNumber", query.getPhoneNumber()));
                }
                if (query.getEnabled() != null) {
                    if (Boolean.TRUE.equals(query.getEnabled())) {
                        predicates.add(cb.equal(root.get("status"), UserStatus.NORMAL.toPersistenceValue()));
                    } else {
                        predicates.add(cb.notEqual(root.get("status"), UserStatus.NORMAL.toPersistenceValue()));
                    }
                }
            }
            predicates.add(cb.equal(root.get("deleted"), false));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return this.userDao.findAll(spec, pageable).map(this.convertor::toAggregate);
    }

    @Override
    public List<UserAggregate> findAll() {
        return this.userDao.findAll().stream().map(this.convertor::toAggregate).toList();
    }

    @Override
    public List<UserAggregate> findActiveUsers() {
        Specification<UserPO> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("status"), UserStatus.NORMAL.toPersistenceValue()),
                cb.equal(root.get("deleted"), false));
        return this.userDao.findAll(spec).stream().map(this.convertor::toAggregate).toList();
    }

    @Override
    public List<UserAggregate> findByDeptId(Long deptId) {
        if (deptId == null) {
            return List.of();
        }
        Specification<UserPO> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("deptId"), deptId),
                cb.equal(root.get("deleted"), false));
        return this.userDao.findAll(spec).stream().map(this.convertor::toAggregate).toList();
    }

    @Override
    public long countActiveUsers() {
        return this.userDao.countByDeletedFalseAndStatus(UserStatus.NORMAL.toPersistenceValue());
    }

    @Override
    public long countOnlineUsers() {
        return this.userDao.countByDeletedFalseAndStatus(UserStatus.NORMAL.toPersistenceValue());
    }

    @Override
    public boolean existsByUsername(Username username) {
        return username != null && this.userDao.existsByUsername(username.value());
    }

    @Override
    public boolean existsByEmail(Email email) {
        return email != null && !email.isBlank() && this.userDao.existsByEmail(email.value());
    }

    @Override
    public boolean existsByPhoneNumber(PhoneNumber phoneNumber) {
        return phoneNumber != null && !phoneNumber.isBlank() && this.userDao.existsByPhoneNumber(phoneNumber.value());
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
        target.setCreateTime(source.getCreateTime());
        target.setCreatorId(source.getCreatorId());
        if (target.getDeleted() == null) {
            target.setDeleted(source.getDeleted());
        }
    }

    private static Predicate like(CriteriaBuilder cb, Root<UserPO> root, String attribute, String value) {
        String pattern = "%" + escapeLike(value) + "%";
        return cb.like(root.get(attribute).as(String.class), pattern, LIKE_ESCAPE_CHAR);
    }

    private static String escapeLike(String value) {
        String escape = String.valueOf(LIKE_ESCAPE_CHAR);
        return value.replace(escape, escape + escape)
                .replace("%", escape + "%")
                .replace("_", escape + "_");
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
