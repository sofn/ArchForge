package com.lesofn.appboot.server.admin.service.user.impl;

import com.lesofn.appboot.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.appboot.server.admin.dto.CurrentLoginUserDTO;
import com.lesofn.appboot.server.admin.service.user.UserService;
import com.lesofn.appboot.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;

/**
 * 用户服务实现类
 *
 * @author lesofn
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserService sysUserService;

    @Override
    public CurrentLoginUserDTO getLoginUserInfo(SystemLoginUser loginUser) {
        CurrentLoginUserDTO userInfo = new CurrentLoginUserDTO();
        userInfo.setUserInfo(loginUser);
        userInfo.setRoleKey("");
        userInfo.setPermissions(new HashSet<>());
        return userInfo;
    }
}