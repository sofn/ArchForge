package com.lesofn.archforge.user.infrastructure.dao;

import com.lesofn.archforge.user.infrastructure.adapter.repository.po.UserPO;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 用户持久化 DAO。
 */
@Repository
public interface UserDao extends JpaRepository<UserPO, Long>, JpaSpecificationExecutor<UserPO> {

    Optional<UserPO> findByUsername(String username);

    Optional<UserPO> findByEmail(String email);

    Optional<UserPO> findByPhoneNumber(String phoneNumber);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    long countByDeletedFalse();

    long countByDeletedFalseAndStatus(Integer status);
}
