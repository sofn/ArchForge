package com.lesofn.appboot.user.service;

import com.lesofn.appboot.user.domain.query.SysUserQuery;
import com.lesofn.appboot.user.dao.SysUserRepository;
import com.lesofn.appboot.user.domain.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Transactional
    public void softDeleteById(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setDeleted(true);
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
        });
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
        userRepository.findById(userId).ifPresent(user -> {
            user.setLoginIp(loginIp);
            user.setLoginDate(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setPassword(newPassword);
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    public Page<SysUser> searchUsers(SysUserQuery query, Pageable pageable) {
        // 简化实现，实际应该根据查询条件进行筛选
        return userRepository.findAll(pageable);
    }

    @Transactional
    public void updateStatus(Long userId, Integer status) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setStatus(status);
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void updatePassword(Long userId, String newPassword) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setPassword(newPassword);
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
        });
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