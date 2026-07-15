package com.lesofn.archforge.user.service;

import com.lesofn.archforge.user.dao.SysUserRepository;
import com.lesofn.archforge.user.domain.SysUser;
import com.lesofn.archforge.user.domain.query.SysUserQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserRepository userRepository;

    public Optional<SysUser> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<SysUser> findByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

    public Optional<SysUser> findByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    public Optional<SysUser> findByPhoneNumber(String phoneNumber) {
        return Optional.ofNullable(userRepository.findByPhoneNumber(phoneNumber));
    }

    public Page<SysUser> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<SysUser> findAll(Specification<SysUser> spec, Pageable pageable) {
        return userRepository.findAll(spec, pageable);
    }

    public List<SysUser> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public SysUser create(SysUser user) {
        user.setPassword(user.getPassword());
        user.setCreateTime(LocalDateTime.now());
        user.setDeleted(false);
        return userRepository.save(user);
    }

    @Transactional
    public SysUser update(SysUser user) {
        user.setUpdateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    private void updateIfPresent(Long id, Consumer<SysUser> updater) {
        userRepository
                .findById(id)
                .ifPresent(
                        user -> {
                            updater.accept(user);
                            user.setUpdateTime(LocalDateTime.now());
                            userRepository.save(user);
                        });
    }

    @Transactional
    public void softDeleteById(Long id) {
        updateIfPresent(id, user -> user.setDeleted(true));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Transactional
    public void updateLoginInfo(Long userId, String loginIp) {
        updateIfPresent(
                userId,
                user -> {
                    user.setLoginIp(loginIp);
                    user.setLoginDate(LocalDateTime.now());
                });
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        updateIfPresent(userId, user -> user.setPassword(newPassword));
    }

    public Page<SysUser> searchUsers(SysUserQuery query, Pageable pageable) {
        // 简化实现，实际应该根据查询条件进行筛选
        return userRepository.findAll(pageable);
    }

    @Transactional
    public void updateStatus(Long userId, Integer status) {
        updateIfPresent(userId, user -> user.setStatus(status));
    }

    @Transactional
    public void updatePassword(Long userId, String newPassword) {
        updateIfPresent(userId, user -> user.setPassword(newPassword));
    }

    public List<SysUser> findActiveUsers() {
        // 假设状态为1表示活跃用户
        // 实际实现应该根据具体业务逻辑调整
        return userRepository.findAll();
    }

    public List<SysUser> findByDeptId(Long deptId) {
        // 简化实现，实际应该根据部门ID查询用户
        return userRepository.findAll();
    }

    public SysUser getUserByUserName(String username) {
        return userRepository.findByUsername(username);
    }
}
