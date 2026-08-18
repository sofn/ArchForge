package ${packageBase}.rest;

import ${packageBase}.dto.*;
import ${packageBase}.service.${entityName}Service;
import com.lesofn.archforge.infrastructure.frame.context.RequestContext;
import com.lesofn.archforge.meta.table.api.dto.ImportResponse;
import com.lesofn.archforge.meta.table.api.enums.MetaDataFormat;
import com.lesofn.archforge.meta.table.api.service.MetaTableCrudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "${tableName}")
@RestController
@RequiredArgsConstructor
@RequestMapping("${basePath}")
public class ${entityName}Controller {

    private static final Long TABLE_ID = ${tableId?c}L;

    private final ${entityName}Service ${entityName?uncap_first}Service;
    private final MetaTableCrudService metaTableCrudService;

    @Operation(summary = "获取${tableName}列表")
    @PostMapping
    public ${entityName}PageResult<${entityName}Response> list(@RequestBody @Valid ${entityName}ListRequest request) {
        return ${entityName?uncap_first}Service.list(request);
    }

    @Operation(summary = "创建${tableName}")
    @PostMapping("/create")
    public Long create(@RequestBody @Valid ${entityName}CreateRequest request) {
        return ${entityName?uncap_first}Service.create(request);
    }

    @Operation(summary = "更新${tableName}")
    @PutMapping("/{id}")
    public Boolean update(@PathVariable Long id, @RequestBody @Valid ${entityName}UpdateRequest request) {
        request.setId(id);
        return ${entityName?uncap_first}Service.update(request);
    }

    @Operation(summary = "删除${tableName}")
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return ${entityName?uncap_first}Service.delete(id);
    }

    @Operation(summary = "获取${tableName}详情")
    @GetMapping("/{id}")
    public ${entityName}Response detail(@PathVariable Long id) {
        return ${entityName?uncap_first}Service.detail(id);
    }

    @Operation(summary = "导出${tableName}数据")
    @GetMapping("/export")
    public void export(@RequestParam(defaultValue = "EXCEL") String format, HttpServletResponse response) throws IOException {
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
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment; filename=${tableCode}_" + TABLE_ID + suffix);
        metaTableCrudService.export(TABLE_ID, dataFormat, response.getOutputStream());
    }

    @Operation(summary = "导入${tableName}数据")
    @PostMapping("/import")
    public ImportResponse importData(RequestContext rc, @RequestParam(defaultValue = "CSV") String format, @RequestPart("file") MultipartFile file) throws IOException {
        return metaTableCrudService.importData(TABLE_ID, MetaDataFormat.of(format), file.getInputStream(), rc.getCurrentUid());
    }
}
