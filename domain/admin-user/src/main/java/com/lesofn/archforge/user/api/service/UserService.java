package com.lesofn.archforge.user.api.service;

import com.lesofn.archforge.user.api.domain.SysUser;
import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<SysUser> findByUsername(String username);

    SysUser saveUser(SysUser user);

    List<SysUser> findAllUsers();

    Optional<SysUser> findById(Long id);
}
