package com.lesofn.archforge.server.admin.controller.admin;

import com.lesofn.archforge.infrastructure.annotation.Log;
import com.lesofn.archforge.server.admin.dto.AdminDeptItemDTO;
import com.lesofn.archforge.server.admin.dto.request.DeptCreateRequest;
import com.lesofn.archforge.server.admin.dto.request.DeptDeleteRequest;
import com.lesofn.archforge.server.admin.dto.request.DeptUpdateRequest;
import com.lesofn.archforge.server.admin.mapper.AdminDeptMapper;
import com.lesofn.archforge.user.domain.SysDept;
import com.lesofn.archforge.user.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "部门管理")
@PreAuthorize("hasRole('ADMIN')")
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class AdminDeptController {

    private final AdminDeptMapper deptMapper;

    @Autowired(required = false)
    private SysDeptService deptService;

    @Operation(summary = "获取全量部门列表")
    @PostMapping("/dept")
    public List<AdminDeptItemDTO> getDeptList() {
        if (deptService == null) {
            return Collections.emptyList();
        }
        List<SysDept> allDepts = deptService.findAll();
        return allDepts.stream().map(deptMapper::toDto).collect(Collectors.toList());
    }

    @Log
    @Operation(summary = "创建部门")
    @PostMapping("/dept/create")
    public Long createDept(@RequestBody @Valid DeptCreateRequest request) {
        SysDept dept = new SysDept();
        dept.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        dept.setName(request.getName());
        dept.setPrincipal(request.getPrincipal() != null ? request.getPrincipal() : "");
        dept.setPhone(request.getPhone() != null ? request.getPhone() : "");
        dept.setEmail(request.getEmail() != null ? request.getEmail() : "");
        dept.setSort(request.getSort() != null ? request.getSort() : 0);
        dept.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        dept.setRemark(request.getRemark() != null ? request.getRemark() : "");
        SysDept saved = deptService.create(dept);
        return saved.getDeptId();
    }

    @Log
    @Operation(summary = "更新部门")
    @PutMapping("/dept/update")
    public Boolean updateDept(@RequestBody @Valid DeptUpdateRequest request) {
        Optional<SysDept> opt = deptService.findById(request.getId());
        if (opt.isEmpty()) {
            return false;
        }
        SysDept dept = opt.get();
        if (request.getName() != null)
            dept.setName(request.getName());
        if (request.getPrincipal() != null)
            dept.setPrincipal(request.getPrincipal());
        if (request.getPhone() != null)
            dept.setPhone(request.getPhone());
        if (request.getEmail() != null)
            dept.setEmail(request.getEmail());
        if (request.getSort() != null)
            dept.setSort(request.getSort());
        if (request.getStatus() != null)
            dept.setStatus(request.getStatus());
        if (request.getRemark() != null)
            dept.setRemark(request.getRemark());
        if (request.getParentId() != null)
            dept.setParentId(request.getParentId());
        deptService.update(dept);
        return true;
    }

    @Log
    @Operation(summary = "删除部门")
    @PostMapping("/dept/delete")
    public Boolean deleteDept(@RequestBody @Valid DeptDeleteRequest request) {
        deptService.deleteById(request.getId());
        return true;
    }
}
