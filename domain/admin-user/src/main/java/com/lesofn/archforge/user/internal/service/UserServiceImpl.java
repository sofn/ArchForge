package com.lesofn.archforge.user.internal.service;

import com.lesofn.archforge.user.api.service.UserService;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.domain.adapter.repository.UserRepository;
import com.lesofn.archforge.user.domain.valueobject.UserId;
import com.lesofn.archforge.user.domain.valueobject.Username;
import com.lesofn.archforge.user.internal.convert.SysUserConvertor;
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

    private final UserRepository userRepository;
    private final SysUserConvertor sysUserConvertor;

    /** 根据用户名查询用户 */
    public Optional<SysUser> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByUsername(new Username(username)).map(sysUserConvertor::toSysUser);
    }

    /** 保存用户 */
    public SysUser saveUser(SysUser user) {
        return sysUserConvertor.toSysUser(userRepository.save(sysUserConvertor.toAggregate(user)));
    }

    /** 查询所有用户 */
    public List<SysUser> findAllUsers() {
        return userRepository.findAll().stream().map(sysUserConvertor::toSysUser).toList();
    }

    /** 根据ID查找用户 */
    public Optional<SysUser> findById(Long id) {
        if (id == null || id <= 0L) {
            return Optional.empty();
        }
        return userRepository.findById(new UserId(id)).map(sysUserConvertor::toSysUser);
    }
}
