package com.lesofn.archforge.user.domain.adapter.repository;

import com.lesofn.archforge.user.domain.model.aggregate.UserAggregate;
import com.lesofn.archforge.user.domain.valueobject.UserId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    public UserAggregate save(UserAggregate aggregate) {
        this.store.put(aggregate.getId(), aggregate);
        return aggregate;
    }

    @Override
    public void deleteById(UserId id) {
        this.store.remove(id);
    }
}
