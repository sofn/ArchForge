package com.lesofn.archforge.server.admin.controller.user;

import com.lesofn.archforge.server.admin.controller.ControllerHelper;
import com.lesofn.archforge.server.admin.dto.AdminPageResponse;
import com.lesofn.archforge.server.admin.dto.BasePageRequest;
import com.lesofn.archforge.server.admin.dto.request.DeleteRequest;
import com.lesofn.archforge.server.admin.dto.response.OperationLogResponse;
import com.lesofn.archforge.user.api.domain.SysOperLog;
import com.lesofn.archforge.user.api.service.SysOperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import org.springframework.web.bind.annotation.*;

@Tag(name = "操作日志")
@SaCheckRole(value = "ADMIN", type = StpAdminUtil.TYPE)
@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/admin/operation-log")
public class OperationLogController {

    private final SysOperLogService operLogService;

    @Operation(summary = "获取操作日志列表")
    @PostMapping
    public AdminPageResponse<OperationLogResponse> getOperationLogsList(@RequestBody BasePageRequest request) {
        int currentPage = request.getCurrentPage() != null ? request.getCurrentPage() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<SysOperLog> page = operLogService.findAll(pageable);
        List<OperationLogResponse> list = page.getContent().stream()
                .filter(o -> !Boolean.TRUE.equals(o.getDeleted()))
                .map(o -> new OperationLogResponse(o.getOperId(), o.getUsername(), o.getModule(), o.getSummary(), o.getIp(), o
                        .getAddress(), o.getSystemName(), o.getBrowser(), o.getStatus(), ControllerHelper.toEpochMilli(o
                                .getOperatingTime())))
                .collect(Collectors.toList());
        return AdminPageResponse.of(list, page.getTotalElements(), pageSize, currentPage);
    }

    @Operation(summary = "删除操作日志")
    @PostMapping("/delete")
    public Boolean deleteOperLog(@RequestBody DeleteRequest data) {
        operLogService.deleteById(data.getId());
        return true;
    }

    @Operation(summary = "清空操作日志")
    @PostMapping("/clear")
    public Boolean clearOperLogs() {
        operLogService.clearAll();
        return true;
    }
}
