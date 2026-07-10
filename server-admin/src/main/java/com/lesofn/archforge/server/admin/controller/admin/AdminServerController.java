package com.lesofn.archforge.server.admin.controller.admin;

import com.lesofn.archforge.server.admin.service.monitor.ServerMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "服务器监控")
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class AdminServerController {

    @Autowired(required = false)
    private ServerMonitorService serverMonitorService;

    @Operation(summary = "获取服务器监控信息")
    @GetMapping("/server-info")
    public Map<String, Object> getServerInfo() {
        if (serverMonitorService == null) {
            return Collections.singletonMap("error", "服务器监控未启用");
        }
        return serverMonitorService.getServerInfo();
    }
}
