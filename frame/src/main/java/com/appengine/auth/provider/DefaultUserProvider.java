package com.appengine.auth.provider;

import com.appengine.auth.model.AuthRequest;
import org.springframework.stereotype.Service;

/**
 * @author sofn
 * @version 1.0 Created at: 2017-07-26 14:54
 */
@Service
public class DefaultUserProvider implements UserProvider {

    @Override
    public boolean isValidUser(long uid) {
        return false;
    }

    @Override
    public boolean checkCanAccess(AuthRequest request, long uid) {
        return false;
    }

    @Override
    public long authUser(String loginName, String password) {
        return 0;
    }
}
