package com.lesofn.archforge.user.domain.adapter.repository;

import com.lesofn.archforge.user.domain.model.aggregate.UserAggregate;
import com.lesofn.archforge.user.domain.model.query.UserQuery;
import com.lesofn.archforge.user.domain.valueobject.Email;
import com.lesofn.archforge.user.domain.valueobject.PhoneNumber;
import com.lesofn.archforge.user.domain.valueobject.UserId;
import com.lesofn.archforge.user.domain.valueobject.UserStatus;
import com.lesofn.archforge.user.domain.valueobject.Username;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * 测试中使用的内存用户仓储实现。
 */
public class InMemoryUserRepository implements UserRepository {

    private final Map<UserId, UserAggregate> store = new HashMap<>();

    @Override
    public Optional<UserAggregate> findById(UserId id) {
        return Optional.ofNullable(this.store.get(id));
    }

    @Override
    public Optional<UserAggregate> findByUsername(Username username) {
        return this.store.values().stream()
                .filter(user -> user.getUser().getUsername().equals(username))
                .findFirst();
    }

    @Override
    public Optional<UserAggregate> findByEmail(Email email) {
        return this.store.values().stream()
                .filter(user -> user.getUser().getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<UserAggregate> findByPhoneNumber(PhoneNumber phoneNumber) {
        return this.store.values().stream()
                .filter(user -> user.getUser().getPhoneNumber().equals(phoneNumber))
                .findFirst();
    }

    @Override
    public UserAggregate save(UserAggregate aggregate) {
        this.store.put(aggregate.getId(), aggregate);
        return aggregate;
    }

    @Override
    public void deleteById(UserId id) {
        this.store.remove(id);
    }

    @Override
    public Page<UserAggregate> findAll(Pageable pageable) {
        return paginate(findAll(), pageable);
    }

    @Override
    public Page<UserAggregate> findAll(Specification<?> spec, Pageable pageable) {
        return findAll(pageable);
    }

    @Override
    public Page<UserAggregate> search(UserQuery query, Pageable pageable) {
        List<UserAggregate> matched = findAll().stream().filter(user -> matches(user, query)).toList();
        return paginate(matched, pageable);
    }

    @Override
    public List<UserAggregate> findAll() {
        return new ArrayList<>(this.store.values());
    }

    @Override
    public List<UserAggregate> findActiveUsers() {
        return findAll().stream().filter(user -> !user.isDeleted() && user.getUser().isActive()).toList();
    }

    @Override
    public List<UserAggregate> findByDeptId(Long deptId) {
        if (deptId == null) {
            return List.of();
        }
        return findAll().stream()
                .filter(user -> !user.isDeleted() && deptId.equals(user.getDeptId()))
                .toList();
    }

    @Override
    public long countActiveUsers() {
        return findAll().stream().filter(user -> !user.isDeleted()).count();
    }

    @Override
    public long countOnlineUsers() {
        return findAll().stream()
                .filter(user -> !user.isDeleted() && user.getUser().getStatus() == UserStatus.NORMAL)
                .count();
    }

    @Override
    public boolean existsByUsername(Username username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public boolean existsByEmail(Email email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public boolean existsByPhoneNumber(PhoneNumber phoneNumber) {
        return findByPhoneNumber(phoneNumber).isPresent();
    }

    private static boolean matches(UserAggregate user, UserQuery query) {
        if (user.isDeleted()) {
            return false;
        }
        if (query == null) {
            return true;
        }
        if (isNotBlank(query.getUsername()) && !contains(user.getUser().getUsername().value(), query.getUsername())) {
            return false;
        }
        if (isNotBlank(query.getEmail()) && !contains(user.getUser().getEmail().value(), query.getEmail())) {
            return false;
        }
        if (isNotBlank(query.getPhoneNumber()) && !contains(user.getUser().getPhoneNumber().value(), query.getPhoneNumber())) {
            return false;
        }
        if (query.getEnabled() != null && query.getEnabled() != user.getUser().isActive()) {
            return false;
        }
        return true;
    }

    private static boolean contains(String value, String fragment) {
        return value != null && value.contains(fragment);
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static Page<UserAggregate> paginate(List<UserAggregate> users, Pageable pageable) {
        int start = Math.min((int) pageable.getOffset(), users.size());
        int end = Math.min(start + pageable.getPageSize(), users.size());
        return new PageImpl<>(users.subList(start, end), pageable, users.size());
    }
}
