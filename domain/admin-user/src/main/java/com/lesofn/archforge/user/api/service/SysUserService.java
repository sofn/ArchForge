package com.lesofn.archforge.user.api.service;

import com.lesofn.archforge.user.api.dao.SysUserRepository;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.api.domain.query.SysUserQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public interface SysUserService {
    Optional<SysUser> findById(Long id);

    Optional<SysUser> findByUsername(String username);

    Optional<SysUser> findByEmail(String email);

    Optional<SysUser> findByPhoneNumber(String phoneNumber);

    Page<SysUser> findAll(Pageable pageable);

    Page<SysUser> findAll(Specification<SysUser> spec, Pageable pageable);

    List<SysUser> findAll();

    SysUser create(SysUser user);

    SysUser update(SysUser user);

    void deleteById(Long id);

    void softDeleteById(Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    void updateLoginInfo(Long userId, String loginIp);

    void resetPassword(Long userId, String newPassword);

    Page<SysUser> searchUsers(SysUserQuery query, Pageable pageable);

    void updateStatus(Long userId, Integer status);

    void updatePassword(Long userId, String newPassword);

    List<SysUser> findActiveUsers();

    List<SysUser> findByDeptId(Long deptId);

    SysUser getUserByUserName(String username);
}
