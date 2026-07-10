package com.lesofn.archforge.server.admin.controller.admin;

import com.lesofn.archforge.server.admin.dto.AdminPageResult;
import com.lesofn.archforge.user.domain.SysOperLog;
import com.lesofn.archforge.user.service.SysOperLogService;
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

@Tag(name = "操作日志")
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class AdminOperationLogController {

    private final SysOperLogService operLogService;

    @Operation(summary = "获取操作日志列表")
    @PostMapping("/operation-logs")
    public AdminPageResult<Map<String, Object>> getOperationLogsList(@RequestBody Map<String, Object> request) {
        int currentPage = AdminControllerHelper.getInt(request, "currentPage", 1);
        int pageSize = AdminControllerHelper.getInt(request, "pageSize", 10);
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<SysOperLog> page = operLogService.findAll(pageable);
        List<Map<String, Object>> list = page.getContent().stream()
                .filter(o -> !Boolean.TRUE.equals(o.getDeleted()))
                .map(o -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", o.getOperId());
                    m.put("username", o.getUsername());
                    m.put("module", o.getModule());
                    m.put("summary", o.getSummary());
                    m.put("ip", o.getIp());
                    m.put("address", o.getAddress());
                    m.put("system", o.getSystemName());
                    m.put("browser", o.getBrowser());
                    m.put("status", o.getStatus());
                    m.put("operatingTime", AdminControllerHelper.toEpochMilli(o.getOperatingTime()));
                    return m;
                })
                .collect(Collectors.toList());
        return AdminPageResult.of(list, page.getTotalElements(), pageSize, currentPage);
    }

    @Operation(summary = "删除操作日志")
    @PostMapping("/operation-logs/delete")
    public Boolean deleteOperLog(@RequestBody Map<String, Object> data) {
        Long id = ((Number) data.get("id")).longValue();
        operLogService.deleteById(id);
        return true;
    }

    @Operation(summary = "清空操作日志")
    @PostMapping("/operation-logs/clear")
    public Boolean clearOperLogs() {
        operLogService.clearAll();
        return true;
    }
}
