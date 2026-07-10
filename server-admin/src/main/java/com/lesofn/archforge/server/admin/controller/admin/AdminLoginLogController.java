package com.lesofn.archforge.server.admin.controller.admin;

import com.lesofn.archforge.server.admin.dto.AdminPageResult;
import com.lesofn.archforge.user.domain.SysLoginLog;
import com.lesofn.archforge.user.service.SysLoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Tag(name = "登录日志")
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class AdminLoginLogController {

    private final SysLoginLogService loginLogService;

    @Operation(summary = "获取登录日志列表")
    @PostMapping("/login-logs")
    public AdminPageResult<Map<String, Object>> getLoginLogsList(@RequestBody Map<String, Object> request) {
        int currentPage = AdminControllerHelper.getInt(request, "currentPage", 1);
        int pageSize = AdminControllerHelper.getInt(request, "pageSize", 10);
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<SysLoginLog> page = loginLogService.findAll(pageable);
        List<Map<String, Object>> list = page.getContent().stream()
                .filter(l -> !Boolean.TRUE.equals(l.getDeleted()))
                .map(l -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", l.getInfoId());
                    m.put("username", l.getUsername());
                    m.put("ip", l.getIp());
                    m.put("address", l.getAddress());
                    m.put("system", l.getSystemName());
                    m.put("browser", l.getBrowser());
                    m.put("status", l.getStatus());
                    m.put("behavior", l.getBehavior());
                    m.put("loginTime", AdminControllerHelper.toEpochMilli(l.getLoginTime()));
                    return m;
                })
                .collect(Collectors.toList());
        return AdminPageResult.of(list, page.getTotalElements(), pageSize, currentPage);
    }

    @Operation(summary = "删除登录日志")
    @PostMapping("/login-logs/delete")
    public Boolean deleteLoginLog(@RequestBody Map<String, Object> data) {
        Long id = ((Number) data.get("id")).longValue();
        loginLogService.deleteById(id);
        return true;
    }

    @Operation(summary = "清空登录日志")
    @PostMapping("/login-logs/clear")
    public Boolean clearLoginLogs() {
        loginLogService.clearAll();
        return true;
    }
}
