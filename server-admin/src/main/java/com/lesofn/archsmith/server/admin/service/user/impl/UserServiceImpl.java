package com.lesofn.archsmith.server.admin.service.user.impl;

import com.lesofn.archsmith.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archsmith.server.admin.dto.CurrentLoginUserDTO;
import com.lesofn.archsmith.server.admin.dto.UserDTO;
import com.lesofn.archsmith.server.admin.service.user.UserService;
import com.lesofn.archsmith.user.domain.SysUser;
import com.lesofn.archsmith.user.service.SysUserService;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        CurrentLoginUserDTO currentUserDTO = new CurrentLoginUserDTO();

        // 根据用户ID查询用户完整信息
        SysUser user = sysUserService.findById(loginUser.getUserId()).orElse(null);

        // 创建UserDTO并设置用户信息
        UserDTO userDTO = new UserDTO(user);
        currentUserDTO.setUserInfo(userDTO);

        // 设置角色key
        if (loginUser.getRoleInfo() != null) {
            currentUserDTO.setRoleKey(loginUser.getRoleInfo().getRoleKey());
        } else {
            currentUserDTO.setRoleKey("");
        }

        // 设置权限列表
        if (loginUser.getRoleInfo() != null
                && loginUser.getRoleInfo().getMenuPermissions() != null) {
            currentUserDTO.setPermissions(loginUser.getRoleInfo().getMenuPermissions());
        } else {
            currentUserDTO.setPermissions(new HashSet<>());
        }

        return currentUserDTO;
    }
}
