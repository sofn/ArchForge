package com.lesofn.archforge.server.admin.controller;

import com.lesofn.archforge.infrastructure.annotation.Log;
import com.lesofn.archforge.infrastructure.frame.context.RequestContext;
import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaTable;
import com.lesofn.archforge.meta.table.api.dto.MetaPageResult;
import com.lesofn.archforge.meta.table.api.service.MetaTableAdminService;
import com.lesofn.archforge.meta.table.api.service.MetaTableCrudService;
import com.lesofn.archforge.meta.table.internal.generator.CodeGenOptions;
import com.lesofn.archforge.meta.table.internal.generator.GeneratedResult;
import com.lesofn.archforge.meta.table.internal.generator.MetaTableCodeGenerator;
import com.lesofn.archforge.server.admin.dto.AdminPageResult;
import com.lesofn.archforge.server.admin.dto.MetaTableResponse;
import com.lesofn.archforge.server.admin.dto.request.MetaDataListRequest;
import com.lesofn.archforge.server.admin.dto.request.MetaTableGenerateRequest;
import com.lesofn.archforge.server.admin.dto.response.MetaTableGenerateResponse;
import java.nio.file.Path;
import com.lesofn.archforge.server.admin.dto.request.MetaTableCreateRequest;
import com.lesofn.archforge.server.admin.dto.request.MetaTableListRequest;
import com.lesofn.archforge.server.admin.dto.request.MetaTableUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import com.lesofn.archforge.server.admin.config.CodeGenWorkspaceResolver;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 元表格管理接口
 */
@Tag(name = "元表格管理")
@PreAuthorize("hasRole('ADMIN')")
@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/meta-table")
public class MetaTableController {

    private final MetaTableAdminService metaTableAdminService;
    private final MetaTableCrudService metaTableCrudService;
    private final MetaTableCodeGenerator metaTableCodeGenerator;
    private final CodeGenWorkspaceResolver codeGenWorkspaceResolver;

    @Operation(summary = "获取元表格列表")
    @PostMapping
    public AdminPageResult<MetaTableResponse> list(@RequestBody MetaTableListRequest request) {
        int currentPage = request.getCurrentPage() != null && request.getCurrentPage() > 0
                ? request.getCurrentPage()
                : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0
                ? request.getPageSize()
                : 10;
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<MetaTable> page = metaTableAdminService.list(request.getKeyword(), pageable);
        List<MetaTableResponse> list = page.getContent().stream()
                .map(MetaTableResponse::of)
                .toList();
        return AdminPageResult.of(list, page.getTotalElements(), pageSize, currentPage);
    }

    @Operation(summary = "获取元表格详情")
    @GetMapping("/{id}")
    public MetaTableResponse detail(@PathVariable Long id) {
        MetaTable table = metaTableAdminService.findById(id);
        List<MetaColumn> columns = metaTableAdminService.findColumns(id);
        return MetaTableResponse.of(table, columns);
    }

    @Log
    @Operation(summary = "创建元表格")
    @PostMapping("/create")
    public Long create(RequestContext rc, @RequestBody @Valid MetaTableCreateRequest request) {
        MetaTable table = request.toTable();
        table.setCreatorId(rc.getCurrentUid());
        List<MetaColumn> columns = request.toColumns();
        for (MetaColumn column : columns) {
            column.setCreatorId(rc.getCurrentUid());
        }
        return metaTableAdminService.create(table, columns);
    }

    @Log
    @Operation(summary = "更新元表格")
    @PutMapping("/{id}")
    public Boolean update(RequestContext rc, @PathVariable Long id, @RequestBody @Valid MetaTableUpdateRequest request) {
        MetaTable table = request.toTable();
        table.setUpdaterId(rc.getCurrentUid());
        metaTableAdminService.update(id, table, null);
        return true;
    }

    @Log
    @Operation(summary = "复制元表格")
    @PostMapping("/{id}/copy")
    public Long copy(@PathVariable Long id) {
        return metaTableAdminService.copy(id);
    }

    @Log
    @Operation(summary = "生成元表格代码")
    @PostMapping("/{id}/generate")
    public MetaTableGenerateResponse generate(@PathVariable Long id, @RequestBody @Valid MetaTableGenerateRequest request) {
        MetaTable table = metaTableAdminService.findById(id);
        List<MetaColumn> columns = metaTableAdminService.findColumns(id);

        String tableCode = table.getTableCode();
        Path projectRoot = codeGenWorkspaceResolver.resolve();

        Path backendDir = codeGenWorkspaceResolver.resolveBackendDir(request.getBackendDir(), tableCode);
        Path frontendDir = codeGenWorkspaceResolver.resolveFrontendDir(request.getFrontendDir(), tableCode);

        String basePath = request.getBasePath();
        if (basePath == null || basePath.isBlank()) {
            basePath = "/generated/" + tableCode;
        }
        boolean overwrite = Boolean.TRUE.equals(request.getOverwrite());

        CodeGenOptions options = new CodeGenOptions();
        options.setProjectRoot(projectRoot);
        options.setBackendOutputDir(backendDir);
        options.setFrontendOutputDir(frontendDir);
        options.setBasePath(basePath);
        options.setOverwrite(overwrite);

        GeneratedResult result = metaTableCodeGenerator.generate(table, columns, options);

        MetaTableGenerateResponse response = new MetaTableGenerateResponse();
        response.setBackendDir(result.getBackendDir().toString());
        response.setFrontendDir(result.getFrontendDir().toString());
        response.setFiles(result.getFiles().size());
        return response;
    }

    @Operation(summary = "检查删除元表格")
    @GetMapping("/{id}/delete-check")
    public Long deleteCheck(@PathVariable Long id) {
        return metaTableAdminService.checkDelete(id);
    }

    @Log
    @Operation(summary = "删除元表格")
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id, @RequestParam(defaultValue = "false") Boolean force) {
        metaTableAdminService.delete(id, Boolean.TRUE.equals(force));
        return true;
    }

    @Operation(summary = "获取元表格数据")
    @PostMapping("/{id}/data")
    public AdminPageResult<Map<String, Object>> listData(
            @PathVariable Long id, @RequestBody MetaDataListRequest request) {
        int currentPage = request.getCurrentPage() != null && request.getCurrentPage() > 0
                ? request.getCurrentPage()
                : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0
                ? request.getPageSize()
                : 10;
        MetaPageResult<Map<String, Object>> result = metaTableCrudService.list(
                id, request.getFilters(), currentPage, pageSize);
        return AdminPageResult.of(result.getList(), result.getTotal(), result.getPageSize(), result.getCurrentPage());
    }

    @Log
    @Operation(summary = "新增元表格数据")
    @PostMapping("/{id}/data/create")
    public Long createData(RequestContext rc, @PathVariable Long id, @RequestBody Map<String, Object> row) {
        return metaTableCrudService.insert(id, row, rc.getCurrentUid());
    }

    @Log
    @Operation(summary = "修改元表格数据")
    @PutMapping("/{id}/data/{dataId}")
    public Boolean updateData(
            RequestContext rc,
            @PathVariable Long id,
            @PathVariable Long dataId,
            @RequestBody Map<String, Object> row) {
        return metaTableCrudService.update(id, dataId, row, rc.getCurrentUid());
    }

    @Log
    @Operation(summary = "删除元表格数据")
    @PostMapping("/{id}/data/{dataId}/delete")
    public Boolean deleteData(RequestContext rc, @PathVariable Long id, @PathVariable Long dataId) {
        return metaTableCrudService.softDelete(id, dataId, rc.getCurrentUid());
    }

    @Operation(summary = "导出元表格数据")
    @GetMapping("/{id}/export")
    public void export(@PathVariable Long id, HttpServletResponse response) throws IOException {
        String fileName = "meta_table_" + id + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        metaTableCrudService.export(id, response.getOutputStream());
    }
}
