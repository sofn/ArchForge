package com.lesofn.archforge.server.admin.controller.admin;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

/**
 * 管理端 Controller 共享工具方法
 */
public final class AdminControllerHelper {

    private AdminControllerHelper() {
    }

    public static Long toEpochMilli(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static int getInt(Map<String, Object> map, String key, int defaultValue) {
        return Optional.ofNullable(map.get(key))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(Number::intValue)
                .orElse(defaultValue);
    }
}
