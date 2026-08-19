package com.lesofn.archforge.infrastructure.auth;

import com.lesofn.archforge.infrastructure.auth.errors.AdminAuthErrorCode;
import com.lesofn.archforge.infrastructure.auth.errors.AdminAuthException;
import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.auth.stp.LoginSessionKeys;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import com.lesofn.archforge.infrastructure.auth.stp.StpWebUtil;
import java.util.Optional;

public final class LoginContext {

    private static final ThreadLocal<Long> OVERRIDE_WEB_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> OVERRIDE_WEB_USERNAME = new ThreadLocal<>();

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
            return Optional.of(restoreAuthorities(loginUser));
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

    public static void setWebUser(Long userId, String username) {
        OVERRIDE_WEB_USER_ID.set(userId);
        OVERRIDE_WEB_USERNAME.set(username);
    }

    public static void clearWebUser() {
        OVERRIDE_WEB_USER_ID.remove();
        OVERRIDE_WEB_USERNAME.remove();
    }

    public static Long getWebUserId() {
        Long override = OVERRIDE_WEB_USER_ID.get();
        if (override != null) {
            return override;
        }
        try {
            return StpWebUtil.isLogin() ? StpWebUtil.getLoginIdAsLong() : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public static String getWebUsername() {
        String override = OVERRIDE_WEB_USERNAME.get();
        if (override != null) {
            return override;
        }
        try {
            if (!StpWebUtil.isLogin()) {
                return null;
            }
            Object username = StpWebUtil.getSession().get(LoginSessionKeys.USERNAME);
            return username instanceof String value ? value : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public static SystemLoginUser restoreAuthorities(SystemLoginUser loginUser) {
        if (loginUser == null) {
            return null;
        }
        if (loginUser.isAdmin()) {
            grantIfAbsent(loginUser, "ROLE_ADMIN");
        }
        grantIfAbsent(loginUser, "ROLE_USER");
        if (loginUser.getRoleInfo() != null && loginUser.getRoleInfo().getMenuPermissions() != null) {
            for (String permission : loginUser.getRoleInfo().getMenuPermissions()) {
                if (permission != null && !permission.isBlank()) {
                    grantIfAbsent(loginUser, permission);
                }
            }
        }
        return loginUser;
    }

    private static void grantIfAbsent(SystemLoginUser loginUser, String authority) {
        loginUser.grantAppPermission(authority);
    }
}
