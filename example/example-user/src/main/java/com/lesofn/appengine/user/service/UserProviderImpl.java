package com.lesofn.appengine.user.service;

import com.lesofn.appengine.auth.model.AuthRequest;
import com.lesofn.appengine.auth.provider.UserProvider;
import com.lesofn.appengine.user.domain.User;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Authors: sofn
 * Version: 1.0  Created at 2015-10-02 22:10.
 */
@Service
public class UserProviderImpl implements UserProvider {
    @Resource
    private UserService userService;

    @Override
    public boolean isValidUser(long uid) {
        return userService.get(uid) != null;
    }

    @Override
    public boolean checkCanAccess(AuthRequest request, long uid) {
        return true;
    }

    @Override
    public long authUser(String loginName, String password) {
        User user = userService.login(loginName, password);
        return user != null ? user.getUid() : 0;
    }
}
