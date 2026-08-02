package com.lesofn.archforge.user.domain.adapter.repository;

import com.lesofn.archforge.user.domain.model.aggregate.UserAggregate;
import com.lesofn.archforge.user.domain.valueobject.UserId;
import java.util.Optional;

/**
 * 用户聚合仓储接口。
 */
public interface UserRepository {

    Optional<UserAggregate> findById(UserId id);

    UserAggregate save(UserAggregate aggregate);

    void deleteById(UserId id);
}
