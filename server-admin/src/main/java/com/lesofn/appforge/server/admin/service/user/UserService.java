package com.lesofn.appforge.server.admin.service.user;

import com.lesofn.appforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.appforge.server.admin.dto.CurrentLoginUserDTO;

/**
 * 用户服务接口
 *
 * @author lesofn
 */
public interface UserService {

    /**
     * 获取登录用户信息
     *
     * @param loginUser 系统登录用户
     * @return 当前登录用户信息
     */
    CurrentLoginUserDTO getLoginUserInfo(SystemLoginUser loginUser);
}
