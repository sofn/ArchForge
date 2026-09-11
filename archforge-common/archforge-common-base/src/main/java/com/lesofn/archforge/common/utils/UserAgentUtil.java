package com.lesofn.archforge.common.utils;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.jspecify.annotations.Nullable;

/**
 * User-Agent 解析工具（yauaa 实现，替代已停更的 eu.bitwalker UserAgentUtils）。
 *
 * <p>
 * {@link UserAgentAnalyzer} 构建成本高且线程安全，全局单例 + LRU 缓存复用。
 */
public final class UserAgentUtil {

    // 只加载所需字段的规则集——yauaa 全量规则加载内存开销大（测试 JVM 易 OOM）
    private static final UserAgentAnalyzer ANALYZER = UserAgentAnalyzer.newBuilder()
            .withFields("AgentName", "OperatingSystemName")
            .hideMatcherLoadStats()
            .withCache(2048)
            .build();

    private UserAgentUtil() {
    }

    /** 解析出的浏览器名与操作系统名；无法解析时 yauaa 返回 "Unknown" 类占位值。 */
    public record AgentInfo(String browser, String operatingSystem) {
    }

    public static AgentInfo parse(@Nullable String userAgentHeader) {
        UserAgent agent = ANALYZER.parse(userAgentHeader == null ? "" : userAgentHeader);
        return new AgentInfo(agent.getValue("AgentName"), agent.getValue("OperatingSystemName"));
    }
}
