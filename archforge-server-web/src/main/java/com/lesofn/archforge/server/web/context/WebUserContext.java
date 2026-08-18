package com.lesofn.archforge.server.web.context;

public final class WebUserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();

    private WebUserContext() {
    }

    public static void set(Long userId, String username) {
        USER_ID.set(userId);
        USERNAME.set(username);
    }

    public static Long getUserId() { return USER_ID.get(); }

    public static String getUsername() { return USERNAME.get(); }

    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
    }
}
