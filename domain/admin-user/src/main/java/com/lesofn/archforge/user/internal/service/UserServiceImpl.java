package com.lesofn.archforge.user.internal.service;

import com.lesofn.archforge.user.api.service.UserService;
import com.lesofn.archforge.user.api.dao.SysUserRepository;
import com.lesofn.archforge.user.api.domain.SysUser;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户服务类
 *
 * @author lesofn
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // TODO(ddd): replace SysUserRepository with UserRepository after SysUser migration
    private final SysUserRepository userRepository;

    /** 根据用户名查询用户 */
    public Optional<SysUser> findByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

    /** 保存用户 */
    public SysUser saveUser(SysUser user) {
        return userRepository.save(user);
    }

    /** 查询所有用户 */
    public List<SysUser> findAllUsers() {
        return userRepository.findAll();
    }

    /** 根据ID查找用户 */
    public Optional<SysUser> findById(Long id) {
        return userRepository.findById(id);
    }
}
