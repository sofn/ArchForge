package com.lesofn.archforge.starter.requestlog;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 请求日志增强 SPI：由宿主应用实现，向 record 回填框架侧字段
 * （如登录 uid、客户端版本、来源 appId）。可为多个，按 @Order 顺序应用。
 */
@FunctionalInterface
public interface RequestLogEnricher {

    void enrich(RequestLogRecord record, HttpServletRequest request);
}
