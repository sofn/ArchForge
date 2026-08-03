package ${packageBase}.rest;

import ${packageBase}.dto.*;
import ${packageBase}.service.${entityName}Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "${tableName}")
@RestController
@RequiredArgsConstructor
@RequestMapping("${basePath}")
public class ${entityName}Controller {

    private final ${entityName}Service ${entityName?uncap_first}Service;

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
}
