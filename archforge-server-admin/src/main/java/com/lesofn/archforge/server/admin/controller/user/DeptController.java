package com.lesofn.archforge.server.admin.controller.user;

import com.lesofn.archforge.infrastructure.annotation.Log;
import com.lesofn.archforge.server.admin.dto.AdminDeptDTO;
import com.lesofn.archforge.server.admin.dto.request.DeptCreateRequest;
import com.lesofn.archforge.server.admin.dto.request.DeptDeleteRequest;
import com.lesofn.archforge.server.admin.dto.request.DeptUpdateRequest;
import com.lesofn.archforge.server.admin.convertor.AdminDeptConvertor;
import com.lesofn.archforge.user.api.domain.SysDept;
import com.lesofn.archforge.user.api.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "部门管理")
@SaCheckLogin(type = StpAdminUtil.TYPE)
@SaCheckRole(value = "ADMIN", type = StpAdminUtil.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/dept")
public class DeptController {

    private final AdminDeptConvertor deptMapper;
    private final SysDeptService deptService;

    @Operation(summary = "获取全量部门列表")
    @PostMapping
    public List<AdminDeptDTO> getDeptList() {
        List<SysDept> allDepts = deptService.findAll();
        return allDepts.stream().map(deptMapper::toDto).collect(Collectors.toList());
    }

    @Log
    @Operation(summary = "创建部门")
    @SaCheckPermission(value = "system:dept:add", type = StpAdminUtil.TYPE)
    @PostMapping("/create")
    public Long createDept(@RequestBody @Valid DeptCreateRequest request) {
        SysDept dept = new SysDept();
        Long parentId = request.getParentId();
        String principal = request.getPrincipal();
        String phone = request.getPhone();
        dept.setParentId(parentId != null ? parentId : 0L);
        dept.setName(request.getName());
        dept.setPrincipal(principal != null ? principal : "");
        dept.setPhone(phone != null ? phone : "");
        String email = request.getEmail();
        Integer sort = request.getSort();
        Integer status = request.getStatus();
        String remark = request.getRemark();
        dept.setEmail(email != null ? email : "");
        dept.setSort(sort != null ? sort : 0);
        dept.setStatus(status != null ? status : 1);
        dept.setRemark(remark != null ? remark : "");
        SysDept saved = deptService.create(dept);
        return saved.getDeptId();
    }

    @Log
    @Operation(summary = "更新部门")
    @SaCheckPermission(value = "system:dept:edit", type = StpAdminUtil.TYPE)
    @PutMapping("/update")
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
    @SaCheckPermission(value = "system:dept:remove", type = StpAdminUtil.TYPE)
    @PostMapping("/delete")
    public Boolean deleteDept(@RequestBody @Valid DeptDeleteRequest request) {
        deptService.deleteById(request.getId());
        return true;
    }
}
