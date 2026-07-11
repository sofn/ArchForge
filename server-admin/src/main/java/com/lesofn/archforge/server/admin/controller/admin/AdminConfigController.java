package com.lesofn.archforge.server.admin.controller.admin;

import com.lesofn.archforge.infrastructure.annotation.Log;
import com.lesofn.archforge.server.admin.dto.AdminPageResult;
import com.lesofn.archforge.server.admin.dto.request.ConfigCreateRequest;
import com.lesofn.archforge.server.admin.dto.request.ConfigDeleteRequest;
import com.lesofn.archforge.server.admin.dto.request.ConfigUpdateRequest;
import com.lesofn.archforge.user.domain.SysConfig;
import com.lesofn.archforge.user.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "参数管理")
@PreAuthorize("hasRole('ADMIN')")
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class AdminConfigController {

    private final SysConfigService configService;

    @Operation(summary = "获取参数列表")
    @PostMapping("/config")
    public AdminPageResult<Map<String, Object>> getConfigList(@RequestBody Map<String, Object> request) {
        int currentPage = AdminControllerHelper.getInt(request, "currentPage", 1);
        int pageSize = AdminControllerHelper.getInt(request, "pageSize", 10);
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<SysConfig> page = configService.findAll(pageable);
        List<Map<String, Object>> list = page.getContent().stream()
                .filter(c -> !Boolean.TRUE.equals(c.getDeleted()))
                .map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", c.getConfigId());
                    m.put("configName", c.getConfigName());
                    m.put("configKey", c.getConfigKey());
                    m.put("configValue", c.getConfigValue());
                    m.put("configType", c.getConfigType());
                    m.put("remark", c.getRemark());
                    m.put("createTime", AdminControllerHelper.toEpochMilli(c.getCreateTime()));
                    return m;
                })
                .collect(Collectors.toList());
        return AdminPageResult.of(list, page.getTotalElements(), pageSize, currentPage);
    }

    @Log
    @Operation(summary = "创建参数")
    @PostMapping("/config/create")
    public Long createConfig(@RequestBody @Valid ConfigCreateRequest request) {
        SysConfig config = new SysConfig();
        config.setConfigName(request.getConfigName());
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setConfigType(request.getConfigType() != null ? request.getConfigType() : 0);
        config.setRemark(request.getRemark() != null ? request.getRemark() : "");
        SysConfig saved = configService.create(config);
        return saved.getConfigId();
    }

    @Log
    @Operation(summary = "更新参数")
    @PutMapping("/config/update")
    public Boolean updateConfig(@RequestBody @Valid ConfigUpdateRequest request) {
        Optional<SysConfig> opt = configService.findById(request.getId());
        if (opt.isEmpty()) {
            return false;
        }
        SysConfig config = opt.get();
        if (request.getConfigName() != null)
            config.setConfigName(request.getConfigName());
        if (request.getConfigKey() != null)
            config.setConfigKey(request.getConfigKey());
        if (request.getConfigValue() != null)
            config.setConfigValue(request.getConfigValue());
        if (request.getConfigType() != null)
            config.setConfigType(request.getConfigType());
        if (request.getRemark() != null)
            config.setRemark(request.getRemark());
        configService.update(config);
        return true;
    }

    @Log
    @Operation(summary = "删除参数")
    @PostMapping("/config/delete")
    public Boolean deleteConfig(@RequestBody @Valid ConfigDeleteRequest request) {
        configService.deleteById(request.getId());
        return true;
    }
}
