package com.lesofn.archforge.server.admin.controller.user;

import com.lesofn.archforge.server.admin.controller.ControllerHelper;
import com.lesofn.archforge.infrastructure.annotation.Log;
import com.lesofn.archforge.server.admin.dto.AdminPageResponse;
import com.lesofn.archforge.server.admin.dto.BasePageRequest;
import com.lesofn.archforge.server.admin.dto.request.ConfigCreateRequest;
import com.lesofn.archforge.server.admin.dto.request.ConfigDeleteRequest;
import com.lesofn.archforge.server.admin.dto.request.ConfigUpdateRequest;
import com.lesofn.archforge.server.admin.dto.response.ConfigResponse;
import com.lesofn.archforge.user.api.domain.SysConfig;
import com.lesofn.archforge.user.api.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/admin/config")
public class ConfigController {

    private final SysConfigService configService;

    @Operation(summary = "获取参数列表")
    @PostMapping
    public AdminPageResponse<ConfigResponse> getConfigList(@RequestBody BasePageRequest request) {
        int currentPage = request.getCurrentPage() != null ? request.getCurrentPage() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<SysConfig> page = configService.findAll(pageable);
        List<ConfigResponse> list = page.getContent().stream()
                .filter(c -> !Boolean.TRUE.equals(c.getDeleted()))
                .map(c -> new ConfigResponse(c.getConfigId(), c.getConfigName(), c.getConfigKey(), c.getConfigValue(), c
                        .getConfigType(), c.getRemark(), ControllerHelper.toEpochMilli(c.getCreateTime())))
                .collect(Collectors.toList());
        return AdminPageResponse.of(list, page.getTotalElements(), pageSize, currentPage);
    }

    @Log
    @Operation(summary = "创建参数")
    @PostMapping("/create")
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
    @PutMapping("/update")
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
    @PostMapping("/delete")
    public Boolean deleteConfig(@RequestBody @Valid ConfigDeleteRequest request) {
        configService.deleteById(request.getId());
        return true;
    }
}
