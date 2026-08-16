package com.lesofn.archforge.server.admin.controller.user;

import com.lesofn.archforge.server.admin.controller.ControllerHelper;
import com.lesofn.archforge.server.admin.dto.AdminPageResponse;
import com.lesofn.archforge.server.admin.dto.BasePageRequest;
import com.lesofn.archforge.server.admin.dto.request.DeleteRequest;
import com.lesofn.archforge.server.admin.dto.response.LoginLogResponse;
import com.lesofn.archforge.user.api.domain.SysLoginLog;
import com.lesofn.archforge.user.api.service.SysLoginLogService;
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
@RequestMapping("/admin/login-log")
public class LoginLogController {

    private final SysLoginLogService loginLogService;

    @Operation(summary = "获取登录日志列表")
    @PostMapping
    public AdminPageResponse<LoginLogResponse> getLoginLogsList(@RequestBody BasePageRequest request) {
        int currentPage = request.getCurrentPage() != null ? request.getCurrentPage() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<SysLoginLog> page = loginLogService.findAll(pageable);
        List<LoginLogResponse> list = page.getContent().stream()
                .filter(l -> !Boolean.TRUE.equals(l.getDeleted()))
                .map(l -> new LoginLogResponse(l.getInfoId(), l.getUsername(), l.getIp(), l.getAddress(), l.getSystemName(), l
                        .getBrowser(), l.getStatus(), l.getBehavior(), ControllerHelper.toEpochMilli(l.getLoginTime())))
                .collect(Collectors.toList());
        return AdminPageResponse.of(list, page.getTotalElements(), pageSize, currentPage);
    }

    @Operation(summary = "删除登录日志")
    @PostMapping("/delete")
    public Boolean deleteLoginLog(@RequestBody DeleteRequest data) {
        loginLogService.deleteById(data.getId());
        return true;
    }

    @Operation(summary = "清空登录日志")
    @PostMapping("/clear")
    public Boolean clearLoginLogs() {
        loginLogService.clearAll();
        return true;
    }
}
