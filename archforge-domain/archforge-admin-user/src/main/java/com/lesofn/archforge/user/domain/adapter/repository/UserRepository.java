package com.lesofn.archforge.user.domain.adapter.repository;

import com.lesofn.archforge.user.domain.model.aggregate.UserAggregate;
import com.lesofn.archforge.user.domain.model.query.UserQuery;
import com.lesofn.archforge.user.domain.valueobject.Email;
import com.lesofn.archforge.user.domain.valueobject.PhoneNumber;
import com.lesofn.archforge.user.domain.valueobject.UserId;
import com.lesofn.archforge.user.domain.valueobject.Username;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * 用户聚合仓储接口。
 */
public interface UserRepository {

    Optional<UserAggregate> findById(UserId id);

    Optional<UserAggregate> findByUsername(Username username);

    Optional<UserAggregate> findByEmail(Email email);

    Optional<UserAggregate> findByPhoneNumber(PhoneNumber phoneNumber);

    UserAggregate save(UserAggregate aggregate);

    void deleteById(UserId id);

    Page<UserAggregate> findAll(Pageable pageable);

    Page<UserAggregate> findAll(Specification<?> spec, Pageable pageable);

    Page<UserAggregate> search(UserQuery query, Pageable pageable);

    List<UserAggregate> findAll();

    List<UserAggregate> findActiveUsers();

    List<UserAggregate> findByDeptId(Long deptId);

    long countActiveUsers();

    long countOnlineUsers();

    boolean existsByUsername(Username username);

    boolean existsByEmail(Email email);

    boolean existsByPhoneNumber(PhoneNumber phoneNumber);
}
