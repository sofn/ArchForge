package com.lesofn.matrixboot.user.service;

import com.lesofn.matrixboot.user.dao.SysUserRepository;
import com.lesofn.matrixboot.user.domain.SysUser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务类
 *
 * @author lesofn
 */
@Service
public class UserService {

    private final SysUserRepository userRepository;

    public UserService(SysUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 根据用户名查询用户
     */
    public Optional<SysUser> findByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

    /**
     * 保存用户
     */
    public SysUser saveUser(SysUser user) {
        return userRepository.save(user);
    }

    /**
     * 查询所有用户
     */
    public List<SysUser> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 根据ID查找用户
     */
    public Optional<SysUser> findById(Long id) {
        return userRepository.findById(id);
    }
}