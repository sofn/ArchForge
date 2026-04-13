package com.lesofn.appforge.user.service;

import com.lesofn.appforge.infrastructure.auth.model.AuthRequest;
import com.lesofn.appforge.infrastructure.auth.provider.UserProvider;
import com.lesofn.appforge.user.domain.SysUser;
import jakarta.annotation.Resource;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Authors: sofn Version: 1.0 Created at 2015-10-02 22:10. */
@Service
public class UserProviderImpl implements UserProvider {
    @Resource private UserService userService;

    @Override
    public boolean isValidUser(long uid) {
        Optional<SysUser> user = userService.findById(uid);
        return user.isPresent();
    }

    @Override
    public boolean checkCanAccess(AuthRequest request, long uid) {
        return true;
    }

    @Override
    public long authUser(String loginName, String password) {
        Optional<SysUser> user = userService.findByUsername(loginName);
        // 简化处理，实际应该验证密码
        return user.map(SysUser::getUserId).orElse(0L);
    }
}
