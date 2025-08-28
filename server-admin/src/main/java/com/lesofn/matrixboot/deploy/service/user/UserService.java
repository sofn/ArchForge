package com.lesofn.matrixboot.deploy.service.user;

import com.lesofn.matrixboot.deploy.dto.CurrentLoginUserDTO;
import com.lesofn.matrixboot.infrastructure.auth.model.SystemLoginUser;

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