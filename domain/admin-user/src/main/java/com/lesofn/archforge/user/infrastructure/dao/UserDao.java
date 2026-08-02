package com.lesofn.archforge.user.infrastructure.dao;

import com.lesofn.archforge.user.infrastructure.adapter.repository.po.UserPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 用户持久化 DAO。
 */
@Repository
public interface UserDao extends JpaRepository<UserPO, Long> {
}
