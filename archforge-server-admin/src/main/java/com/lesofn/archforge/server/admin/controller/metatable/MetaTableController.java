package com.lesofn.archforge.server.admin.controller.metatable;

import com.lesofn.archforge.infrastructure.annotation.Log;
import com.lesofn.archforge.infrastructure.auth.LoginContext;
import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import com.lesofn.archforge.infrastructure.security.datascope.DataPermission;
import com.lesofn.archforge.meta.table.api.dto.ImportResponse;
import com.lesofn.archforge.meta.table.api.dto.MetaDataQuery;
import com.lesofn.archforge.meta.table.api.dto.MetaPageResponse;
import com.lesofn.archforge.meta.table.api.enums.MetaDataFormat;
import com.lesofn.archforge.meta.table.api.service.MetaTableCrudService;
import com.lesofn.archforge.server.admin.dto.AdminPageResponse;
import com.lesofn.archforge.server.admin.dto.request.MetaDataListRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 元表格运行期数据接口（动态 CRUD / 导入导出）。
 *
 * <p>
 * 常驻装配——运行期能力（meta-runtime）。设计期端点（schema 管理 / 代码生成等）在
 * {@link MetaTableDesignerController}，由 {@code arch-forge.designer.enabled} 门控。
 */
@Tag(name = "元表格数据")
@SaCheckLogin(type = StpAdminUtil.TYPE)
@SaCheckRole(value = "ADMIN", type = StpAdminUtil.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/meta-table")
public class MetaTableController {

    private final MetaTableCrudService metaTableCrudService;
    private final ArchForgeProperties archForgeProperties;

    @Operation(summary = "获取元表格数据")
    @SaCheckPermission(value = "meta-table:list", type = StpAdminUtil.TYPE)
    @DataPermission
    @PostMapping("/{id}/data")
    public AdminPageResponse<Map<String, Object>> listData(
            @PathVariable Long id, @RequestBody MetaDataListRequest request) {
        int currentPage = request.getCurrentPage() != null && request.getCurrentPage() > 0
                ? request.getCurrentPage()
                : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0
                ? request.getPageSize()
                : 10;
        pageSize = Math.min(pageSize, Math.max(archForgeProperties.getMetaTable().getMaxPageSize(), 1));
        boolean skipCount = Boolean.TRUE.equals(request.getSkipCount());
        MetaDataQuery query = new MetaDataQuery(request.getFilters(), currentPage, pageSize, request.getOrderBy(), request
                .getOrderDir(), skipCount);
        MetaPageResponse<Map<String, Object>> result = metaTableCrudService.list(id, query);
        return AdminPageResponse.of(result.getList(), result.getTotal(), result.getPageSize(), result.getCurrentPage());
    }

    @Log
    @Operation(summary = "新增元表格数据")
    @SaCheckPermission(value = "meta-table:add", type = StpAdminUtil.TYPE)
    @DataPermission
    @PostMapping("/{id}/data/create")
    public Long createData(@PathVariable Long id, @RequestBody Map<String, Object> row) {
        return metaTableCrudService.insert(id, row, LoginContext.getAdminUserId());
    }

    @Log
    @Operation(summary = "修改元表格数据")
    @SaCheckPermission(value = "meta-table:edit", type = StpAdminUtil.TYPE)
    @DataPermission
    @PutMapping("/{id}/data/{dataId}")
    public Boolean updateData(
            @PathVariable Long id,
            @PathVariable Long dataId,
            @RequestBody Map<String, Object> row) {
        return metaTableCrudService.update(id, dataId, row, LoginContext.getAdminUserId());
    }

    @Log
    @Operation(summary = "删除元表格数据")
    @SaCheckPermission(value = "meta-table:remove", type = StpAdminUtil.TYPE)
    @DataPermission
    @PostMapping("/{id}/data/{dataId}/delete")
    public Boolean deleteData(@PathVariable Long id, @PathVariable Long dataId) {
        return metaTableCrudService.softDelete(id, dataId, LoginContext.getAdminUserId());
    }

    @Operation(summary = "导出元表格数据")
    @DataPermission
    @GetMapping("/{id}/export")
    public void export(
            @PathVariable Long id,
            @RequestParam(defaultValue = "EXCEL") String format,
            HttpServletResponse response) throws IOException {
        MetaDataFormat dataFormat = MetaDataFormat.of(format);
        String suffix = switch (dataFormat) {
            case CSV -> ".csv";
            case JSON -> ".json";
            default -> ".xlsx";
        };
        String contentType = switch (dataFormat) {
            case CSV -> "text/csv";
            case JSON -> "application/json";
            default -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        };
        String fileName = "meta_table_" + id + suffix;
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        metaTableCrudService.export(id, dataFormat, response.getOutputStream());
    }

    @Log
    @Operation(summary = "导入元表格数据")
    @SaCheckPermission(value = "meta-table:add", type = StpAdminUtil.TYPE)
    @DataPermission
    @PostMapping("/{id}/import")
    public ImportResponse importData(
            @PathVariable Long id,
            @RequestParam(defaultValue = "CSV") String format,
            @RequestPart("file") MultipartFile file) throws IOException {
        MetaDataFormat dataFormat = MetaDataFormat.of(format);
        return metaTableCrudService.importData(id, dataFormat, file.getInputStream(), LoginContext.getAdminUserId());
    }
}
