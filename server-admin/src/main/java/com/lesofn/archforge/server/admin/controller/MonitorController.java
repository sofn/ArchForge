package com.lesofn.archforge.server.admin.controller;

import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.db.redis.RedisUtil;
import com.lesofn.archforge.infrastructure.user.base.LoginInfo;
import com.lesofn.archforge.server.admin.dto.AdminPageResult;
import com.lesofn.archforge.server.admin.service.cache.CacheKeyEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * 系统监控接口：在线用户、缓存信息。
 */
@Tag(name = "系统监控")
@RestController
public class MonitorController {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Operation(summary = "在线用户列表")
    @PostMapping("/online-logs")
    public AdminPageResult<Map<String, Object>> getOnlineLogsList(@RequestBody Map<String, Object> request) {
        int currentPage = getInt(request, "currentPage", 1);
        int pageSize = getInt(request, "pageSize", 10);
        String username = request.get("username") == null ? "" : String.valueOf(request.get("username"));

        Collection<String> keys = redisUtil.keys(CacheKeyEnum.LOGIN_USER_KEY.key() + "*");
        List<SystemLoginUser> all = new ArrayList<>();
        if (keys != null) {
            for (String key : keys) {
                SystemLoginUser user = redisUtil.getCacheObject(key);
                if (user != null && (username == null || username.isBlank() || containsCaseInsensitive(user.getUsername(),
                        username))) {
                    all.add(user);
                }
            }
        }

        all.sort(Comparator.comparing(
                (SystemLoginUser u) -> u.getLoginInfo() != null ? u.getLoginInfo().getLoginTime() : 0L,
                Comparator.reverseOrder()));

        int total = all.size();
        int start = Math.min((currentPage - 1) * pageSize, total);
        int end = Math.min(start + pageSize, total);

        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = start; i < end; i++) {
            SystemLoginUser u = all.get(i);
            LoginInfo info = u.getLoginInfo();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getCachedKey());
            m.put("username", u.getUsername());
            m.put("ip", info != null ? info.getIpAddress() : null);
            m.put("address", info != null ? info.getLocation() : null);
            m.put("system", info != null ? info.getOperationSystem() : null);
            m.put("browser", info != null ? info.getBrowser() : null);
            m.put("loginTime", info != null ? info.getLoginTime() : null);
            list.add(m);
        }

        return AdminPageResult.of(list, total, pageSize, currentPage);
    }

    @Operation(summary = "缓存监控信息")
    @GetMapping("/cache-info")
    public Map<String, Object> getCacheInfo() {
        Properties info = stringRedisTemplate.execute(
                (RedisCallback<Properties>) connection -> connection.serverCommands().info());
        Long dbSize = stringRedisTemplate.execute(
                (RedisCallback<Long>) connection -> connection.serverCommands().dbSize());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dbSize", dbSize == null ? 0 : dbSize);
        if (info != null) {
            result.put("usedMemory", info.getProperty("used_memory"));
            result.put("usedMemoryHuman", info.getProperty("used_memory_human"));
            result.put("connectedClients", info.getProperty("connected_clients"));
            result.put("instantaneousOpsPerSec", info.getProperty("instantaneous_ops_per_sec"));
            result.put("totalCommandsProcessed", info.getProperty("total_commands_processed"));
            result.put("keyspaceHits", info.getProperty("keyspace_hits"));
            result.put("keyspaceMisses", info.getProperty("keyspace_misses"));
            result.put("info", buildInfoString(info));
        }
        return result;
    }

    private static boolean containsCaseInsensitive(String str, String search) {
        if (str == null || search == null) {
            return false;
        }
        return str.toLowerCase().contains(search.toLowerCase());
    }

    private String buildInfoString(Properties info) {
        return info.stringPropertyNames().stream()
                .sorted()
                .map(k -> k + ": " + info.getProperty(k))
                .collect(Collectors.joining("\n"));
    }

    private int getInt(Map<String, Object> request, String key, int defaultValue) {
        Object value = request.get(key);
        if (value instanceof Number n) {
            return n.intValue();
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }
}
