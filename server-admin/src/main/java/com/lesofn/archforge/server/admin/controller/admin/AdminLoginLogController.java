package com.lesofn.archforge.server.admin.controller.admin;

import com.lesofn.archforge.server.admin.dto.AdminPageResult;
import com.lesofn.archforge.server.admin.dto.PageQuery;
import com.lesofn.archforge.server.admin.dto.request.DeleteRequest;
import com.lesofn.archforge.server.admin.dto.response.LoginLogResponse;
import com.lesofn.archforge.user.domain.SysLoginLog;
import com.lesofn.archforge.user.service.SysLoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "登录日志")
@PreAuthorize("hasRole('ADMIN')")
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class AdminLoginLogController {

    private final SysLoginLogService loginLogService;

    @Operation(summary = "获取登录日志列表")
    @PostMapping("/login-logs")
    public AdminPageResult<LoginLogResponse> getLoginLogsList(@RequestBody PageQuery request) {
        int currentPage = request.getCurrentPage() != null ? request.getCurrentPage() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<SysLoginLog> page = loginLogService.findAll(pageable);
        List<LoginLogResponse> list = page.getContent().stream()
                .filter(l -> !Boolean.TRUE.equals(l.getDeleted()))
                .map(l -> new LoginLogResponse(l.getInfoId(), l.getUsername(), l.getIp(), l.getAddress(), l.getSystemName(), l
                        .getBrowser(), l.getStatus(), l.getBehavior(), AdminControllerHelper.toEpochMilli(l.getLoginTime())))
                .collect(Collectors.toList());
        return AdminPageResult.of(list, page.getTotalElements(), pageSize, currentPage);
    }

    @Operation(summary = "删除登录日志")
    @PostMapping("/login-logs/delete")
    public Boolean deleteLoginLog(@RequestBody DeleteRequest data) {
        loginLogService.deleteById(data.getId());
        return true;
    }

    @Operation(summary = "清空登录日志")
    @PostMapping("/login-logs/clear")
    public Boolean clearLoginLogs() {
        loginLogService.clearAll();
        return true;
    }
}
