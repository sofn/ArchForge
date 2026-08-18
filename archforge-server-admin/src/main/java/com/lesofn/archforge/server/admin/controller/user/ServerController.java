package com.lesofn.archforge.server.admin.controller.user;

import com.lesofn.archforge.server.admin.dto.response.ServerInfoResponse;
import com.lesofn.archforge.server.admin.service.monitor.ServerMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import org.springframework.web.bind.annotation.*;

@Tag(name = "服务器监控")
@SaCheckRole(value = "ADMIN", type = StpAdminUtil.TYPE)
@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/admin/server")
public class ServerController {

    @Autowired(required = false)
    private ServerMonitorService serverMonitorService;

    @Operation(summary = "获取服务器监控信息")
    @GetMapping
    public ServerInfoResponse getServerInfo() {
        if (serverMonitorService == null) {
            return ServerInfoResponse.builder().error("服务器监控未启用").build();
        }
        Map<String, Object> info = serverMonitorService.getServerInfo();
        return ServerInfoResponse.builder()
                .cpu(info.get("cpu"))
                .memory(info.get("memory"))
                .jvm(info.get("jvm"))
                .os(info.get("os"))
                .disks(info.get("disks"))
                .error(info.get("error") != null ? info.get("error").toString() : null)
                .build();
    }
}
