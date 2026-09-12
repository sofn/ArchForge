package com.lesofn.archforge.user.internal.service;

import com.lesofn.archforge.user.api.dao.SysUserRepository;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.api.service.SysUserService;
import com.lesofn.archforge.user.api.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
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

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]{2,64}$");

    private final SysUserRepository userRepository;
    private final SysUserService sysUserService;

    /** 根据用户名查询用户 */
    @Override
    public Optional<SysUser> findByUsername(String username) {
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            return Optional.empty();
        }
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

    /** 保存用户 */
    @Override
    public SysUser saveUser(SysUser user) {
        return sysUserService.update(user);
    }

    /** 查询所有用户 */
    @Override
    public List<SysUser> findAllUsers() {
        return userRepository.findAll();
    }

    /** 根据ID查找用户 */
    @Override
    public Optional<SysUser> findById(Long id) {
        if (id == null || id <= 0L) {
            return Optional.empty();
        }
        return userRepository.findById(id);
    }
}
