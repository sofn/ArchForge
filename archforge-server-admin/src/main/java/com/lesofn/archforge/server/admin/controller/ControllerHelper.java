package com.lesofn.archforge.server.admin.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 管理端 Controller 共享工具方法
 */
public final class ControllerHelper {

    private ControllerHelper() {
    }

    public static Long toEpochMilli(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
