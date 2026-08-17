package com.lesofn.archforge.infrastructure.auth;

import com.lesofn.archforge.infrastructure.auth.errors.AdminAuthErrorCode;
import com.lesofn.archforge.infrastructure.auth.errors.AdminAuthException;
import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.auth.stp.LoginSessionKeys;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import com.lesofn.archforge.infrastructure.auth.stp.StpWebUtil;
import java.util.Optional;

public final class LoginContext {

    private LoginContext() {
    }

    public static SystemLoginUser getAdminUser() {
        return findAdminUser().orElseThrow(() -> new AdminAuthException(AdminAuthErrorCode.USER_FAIL_TO_GET_USER_INFO));
    }

    public static Optional<SystemLoginUser> findAdminUser() {
        if (!StpAdminUtil.isLogin()) {
            return Optional.empty();
        }
        Object value = StpAdminUtil.getSession().get(LoginSessionKeys.LOGIN_USER);
        if (value instanceof SystemLoginUser loginUser) {
            return Optional.of(loginUser);
        }
        return Optional.empty();
    }

    public static Long getAdminUserId() {
        try {
            return getAdminUser().getUserId();
        } catch (AdminAuthException e) {
            throw new AdminAuthException(AdminAuthErrorCode.USER_FAIL_TO_GET_USER_ID);
        }
    }

    public static boolean isAdmin() { return findAdminUser().map(SystemLoginUser::isAdmin).orElse(false); }

    public static Long getWebUserId() { return StpWebUtil.isLogin() ? StpWebUtil.getLoginIdAsLong() : null; }

    public static String getWebUsername() {
        if (!StpWebUtil.isLogin()) {
            return null;
        }
        Object username = StpWebUtil.getSession().get(LoginSessionKeys.USERNAME);
        return username instanceof String value ? value : null;
    }
}
