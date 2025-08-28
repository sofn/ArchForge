package com.lesofn.matrixboot.deploy.service.user.impl;

import com.lesofn.matrixboot.deploy.dto.CurrentLoginUserDTO;
import com.lesofn.matrixboot.deploy.service.user.UserService;
import com.lesofn.matrixboot.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.matrixboot.user.domain.SysUser;
import com.lesofn.matrixboot.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        // 这里简化实现，实际应该从loginUser中获取用户信息
        CurrentLoginUserDTO userInfo = new CurrentLoginUserDTO();
        userInfo.setUserId(1L);
        userInfo.setUsername("admin");
        userInfo.setNickName("管理员");
        userInfo.setEmail("admin@example.com");
        userInfo.setPhoneNumber("13800138000");
        userInfo.setSex("男");
        userInfo.setAvatar("");
        userInfo.setDeptId(1L);
        userInfo.setDeptName("系统管理部");
        userInfo.setRoles(new ArrayList<>());
        userInfo.setPermissions(new HashSet<>());
        return userInfo;
    }
}