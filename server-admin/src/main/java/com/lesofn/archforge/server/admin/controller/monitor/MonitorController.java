package com.lesofn.archforge.server.admin.controller.monitor;

import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.infrastructure.db.redis.RedisUtil;
import com.lesofn.archforge.infrastructure.user.base.LoginInfo;
import com.lesofn.archforge.server.admin.dto.AdminPageResponse;
import com.lesofn.archforge.server.admin.dto.request.OnlineLogListRequest;
import com.lesofn.archforge.server.admin.dto.response.CacheInfoResponse;
import com.lesofn.archforge.server.admin.dto.response.OnlineUserResponse;
import com.lesofn.archforge.server.admin.service.cache.CacheKeyEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 系统监控接口：在线用户、缓存信息。
 */
@Tag(name = "系统监控")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor")
public class MonitorController {

    private final RedisUtil redisUtil;
    private final StringRedisTemplate stringRedisTemplate;

    @Operation(summary = "在线用户列表")
    @PostMapping("/online-logs")
    public AdminPageResponse<OnlineUserResponse> getOnlineLogsList(@RequestBody OnlineLogListRequest request) {
        int currentPage = request.getCurrentPage() != null ? request.getCurrentPage() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
        String username = Optional.ofNullable(request.getUsername()).orElse("");

        Collection<String> keys = redisUtil.keys(CacheKeyEnum.LOGIN_USER_KEY.key() + "*");
        List<SystemLoginUser> all = new ArrayList<>();
        if (keys != null) {
            for (String key : keys) {
                SystemLoginUser user = redisUtil.getCacheObject(key);
                if (user != null && (username.isBlank() || containsCaseInsensitive(user.getUsername(), username))) {
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

        List<OnlineUserResponse> list = new ArrayList<>();
        for (int i = start; i < end; i++) {
            SystemLoginUser u = all.get(i);
            LoginInfo info = u.getLoginInfo();
            list.add(new OnlineUserResponse(u.getCachedKey(), u.getUsername(), info != null ? info.getIpAddress()
                    : null, info != null ? info.getLocation() : null, info != null ? info.getOperationSystem()
                            : null, info != null ? info.getBrowser() : null, info != null ? info.getLoginTime() : null));
        }

        return AdminPageResponse.of(list, total, pageSize, currentPage);
    }

    @Operation(summary = "缓存监控信息")
    @GetMapping("/cache-info")
    public CacheInfoResponse getCacheInfo() {
        Properties info = stringRedisTemplate.execute(
                (RedisCallback<Properties>) connection -> connection.serverCommands().info());
        Long dbSize = stringRedisTemplate.execute(
                (RedisCallback<Long>) connection -> connection.serverCommands().dbSize());

        CacheInfoResponse result = new CacheInfoResponse();
        result.setDbSize(dbSize == null ? 0 : dbSize);
        if (info != null) {
            result.setUsedMemory(info.getProperty("used_memory"));
            result.setUsedMemoryHuman(info.getProperty("used_memory_human"));
            result.setConnectedClients(info.getProperty("connected_clients"));
            result.setInstantaneousOpsPerSec(info.getProperty("instantaneous_ops_per_sec"));
            result.setTotalCommandsProcessed(info.getProperty("total_commands_processed"));
            result.setKeyspaceHits(info.getProperty("keyspace_hits"));
            result.setKeyspaceMisses(info.getProperty("keyspace_misses"));
            result.setInfo(buildInfoString(info));
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

}
