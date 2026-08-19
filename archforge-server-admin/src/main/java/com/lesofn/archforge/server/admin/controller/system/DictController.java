package com.lesofn.archforge.server.admin.controller.system;

import com.lesofn.archforge.infrastructure.annotation.Log;
import com.lesofn.archforge.server.admin.dto.AdminPageResponse;
import com.lesofn.archforge.server.admin.dto.dict.*;
import com.lesofn.archforge.user.api.domain.dict.SysDictItem;
import com.lesofn.archforge.user.api.domain.dict.SysDictType;
import com.lesofn.archforge.user.api.service.dict.SysDictService;
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
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import org.springframework.web.bind.annotation.*;

@Tag(name = "字典配置")
@SaCheckLogin(type = StpAdminUtil.TYPE)
@SaCheckRole(value = "ADMIN", type = StpAdminUtil.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dict")
public class DictController {

    private final SysDictService dictService;

    @Operation(summary = "字典类型分页列表")
    @SaCheckPermission(value = "system:dict:list", type = StpAdminUtil.TYPE)
    @PostMapping("/type")
    public AdminPageResponse<DictTypeResponse> listTypes(@RequestBody DictTypeQueryRequest request) {
        int currentPage = request.getCurrentPage() != null && request.getCurrentPage() > 0
                ? request.getCurrentPage()
                : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0
                ? request.getPageSize()
                : 10;
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<SysDictType> page = dictService.findTypePage(request.getKeyword(), pageable);
        List<DictTypeResponse> list = page.getContent().stream()
                .filter(t -> !Boolean.TRUE.equals(t.getDeleted()))
                .map(this::toTypeResponse)
                .toList();
        return AdminPageResponse.of(list, page.getTotalElements(), pageSize, currentPage);
    }

    @Operation(summary = "根据字典编码查询详情及全部项")
    @SaCheckPermission(value = "system:dict:query", type = StpAdminUtil.TYPE)
    @GetMapping("/type/{dictCode}")
    public DictTypeResponse getTypeByCode(@PathVariable String dictCode) {
        Optional<SysDictType> type = dictService.findTypeByCode(dictCode);
        if (type.isEmpty() || Boolean.TRUE.equals(type.get().getDeleted())) {
            return null;
        }
        DictTypeResponse response = toTypeResponse(type.get());
        List<DictItemResponse> items = dictService.findItemsByTypeCode(dictCode).stream()
                .filter(i -> !Boolean.TRUE.equals(i.getDeleted()))
                .map(this::toItemResponse)
                .toList();
        response.setItems(items);
        return response;
    }

    @Log(module = "字典配置", summary = "创建字典类型")
    @Operation(summary = "创建字典类型")
    @SaCheckPermission(value = "system:dict:add", type = StpAdminUtil.TYPE)
    @PostMapping("/type/create")
    public Long createType(@RequestBody @Valid DictTypeCreateRequest request) {
        SysDictType type = new SysDictType()
                .setDictCode(request.getDictCode())
                .setDictName(request.getDictName())
                .setDescription(request.getDescription())
                .setStatus(request.getStatus())
                .setSort(request.getSort());
        List<SysDictItem> items = request.getItems() == null ? List.of()
                : request.getItems().stream()
                        .map(this::toItemDomain)
                        .toList();
        SysDictType saved = dictService.saveTypeWithItems(type, items);
        return saved.getDictTypeId();
    }

    @Log(module = "字典配置", summary = "更新字典类型")
    @Operation(summary = "更新字典类型")
    @SaCheckPermission(value = "system:dict:edit", type = StpAdminUtil.TYPE)
    @PutMapping("/type/{id}")
    public Boolean updateType(@PathVariable Long id, @RequestBody @Valid DictTypeUpdateRequest request) {
        Optional<SysDictType> opt = dictService.findTypeById(id);
        if (opt.isEmpty()) {
            return false;
        }
        SysDictType type = opt.get();
        type.setDictName(request.getDictName());
        type.setDescription(request.getDescription());
        type.setStatus(request.getStatus());
        type.setSort(request.getSort());
        dictService.saveType(type);
        return true;
    }

    @Log(module = "字典配置", summary = "删除字典类型")
    @Operation(summary = "删除字典类型")
    @SaCheckPermission(value = "system:dict:remove", type = StpAdminUtil.TYPE)
    @DeleteMapping("/type/{id}")
    public Boolean deleteType(@PathVariable Long id) {
        dictService.deleteType(id);
        return true;
    }

    @Log(module = "字典配置", summary = "创建字典项")
    @Operation(summary = "创建字典项")
    @SaCheckPermission(value = "system:dict:add", type = StpAdminUtil.TYPE)
    @PostMapping("/type/{typeId}/item")
    public Long createItem(@PathVariable Long typeId, @RequestBody @Valid DictItemRequest request) {
        SysDictItem item = toItemDomain(request);
        SysDictItem saved = dictService.saveItem(typeId, item);
        return saved.getDictItemId();
    }

    @Log(module = "字典配置", summary = "更新字典项")
    @Operation(summary = "更新字典项")
    @PutMapping("/item/{id}")
    public Boolean updateItem(@PathVariable Long id, @RequestBody @Valid DictItemRequest request) {
        Optional<SysDictItem> opt = dictService.findItemById(id);
        if (opt.isEmpty() || Boolean.TRUE.equals(opt.get().getDeleted())) {
            return false;
        }
        SysDictItem item = opt.get();
        item.setItemCode(request.getItemCode());
        item.setItemLabel(request.getItemLabel());
        item.setSort(request.getSort());
        item.setStatus(request.getStatus());
        dictService.saveItem(item.getDictTypeId(), item);
        return true;
    }

    @Log(module = "字典配置", summary = "删除字典项")
    @Operation(summary = "删除字典项")
    @DeleteMapping("/item/{id}")
    public Boolean deleteItem(@PathVariable Long id) {
        dictService.deleteItem(id);
        return true;
    }

    private DictTypeResponse toTypeResponse(SysDictType type) {
        return DictTypeResponse.builder()
                .id(type.getDictTypeId())
                .dictCode(type.getDictCode())
                .dictName(type.getDictName())
                .description(type.getDescription())
                .status(type.getStatus())
                .sort(type.getSort())
                .createTime(type.getCreateTime())
                .updateTime(type.getUpdateTime())
                .build();
    }

    private DictItemResponse toItemResponse(SysDictItem item) {
        return DictItemResponse.builder()
                .id(item.getDictItemId())
                .dictTypeId(item.getDictTypeId())
                .itemCode(item.getItemCode())
                .itemLabel(item.getItemLabel())
                .sort(item.getSort())
                .status(item.getStatus())
                .build();
    }

    private SysDictItem toItemDomain(DictItemRequest request) {
        return new SysDictItem()
                .setDictItemId(request.getId())
                .setItemCode(request.getItemCode())
                .setItemLabel(request.getItemLabel())
                .setSort(request.getSort())
                .setStatus(request.getStatus());
    }
}
