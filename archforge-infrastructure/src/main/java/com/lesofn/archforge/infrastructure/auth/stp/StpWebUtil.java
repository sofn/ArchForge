package com.lesofn.archforge.infrastructure.auth.stp;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;

public final class StpWebUtil {

    public static final String TYPE = "web";
    public static final StpLogic STP_LOGIC = new StpLogic(TYPE);

    private StpWebUtil() {
    }

    public static void login(Object id) {
        STP_LOGIC.login(id);
    }

    public static void login(Object id, SaLoginParameter parameter) {
        STP_LOGIC.login(id, parameter);
    }

    public static void logout() {
        STP_LOGIC.logout();
    }

    public static boolean isLogin() { return STP_LOGIC.isLogin(); }

    public static void checkLogin() {
        STP_LOGIC.checkLogin();
    }

    public static long getLoginIdAsLong() { return STP_LOGIC.getLoginIdAsLong(); }

    public static SaSession getSession() { return STP_LOGIC.getSession(); }

    public static SaSession getSessionByLoginId(Object loginId, boolean isCreate) {
        return STP_LOGIC.getSessionByLoginId(loginId, isCreate);
    }

    public static String getTokenValue() { return STP_LOGIC.getTokenValue(); }

    public static String getTokenName() { return STP_LOGIC.getTokenName(); }

    public static long getTokenTimeout() { return STP_LOGIC.getTokenTimeout(); }
}
