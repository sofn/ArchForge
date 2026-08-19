package com.lesofn.archforge.demo.task.internal.web;

import com.lesofn.archforge.demo.task.api.dto.*;
import com.lesofn.archforge.demo.task.internal.service.TaskService;
import com.lesofn.archforge.infrastructure.frame.context.RequestContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import org.springframework.web.bind.annotation.*;

/** Task management REST API */
@Slf4j
@Tag(name = "任务管理")
@SaCheckRole(value = "ADMIN", type = StpAdminUtil.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "获取任务列表")
    @PostMapping
    public TaskPageResponse<TaskDTO> list(
            RequestContext rc, @RequestBody @Valid TaskListRequest request) {
        return taskService.searchTasks(request);
    }

    @Operation(summary = "创建任务")
    @PostMapping("/create")
    public Long create(RequestContext rc, @RequestBody @Valid TaskCreateRequest request) {
        return taskService.createTask(request, request.getUid() != null ? request.getUid() : rc.getCurrentUid());
    }

    @Operation(summary = "更新任务")
    @PutMapping("/update")
    public Boolean update(@RequestBody @Valid TaskUpdateRequest request) {
        return taskService.updateTask(request);
    }

    @Operation(summary = "删除任务")
    @PostMapping("/delete")
    public Boolean delete(@RequestBody @Valid TaskDeleteRequest request) {
        return taskService.deleteTask(request.getId());
    }

    @Operation(summary = "开始任务")
    @PostMapping("/start")
    public Boolean start(@RequestBody @Valid TaskActionRequest request) {
        return taskService.startTask(request.getId());
    }

    @Operation(summary = "完成任务")
    @PostMapping("/complete")
    public Boolean complete(@RequestBody @Valid TaskActionRequest request) {
        return taskService.completeTask(request.getId());
    }

    @Operation(summary = "取消任务")
    @PostMapping("/cancel")
    public Boolean cancel(@RequestBody @Valid TaskActionRequest request) {
        return taskService.cancelTask(request.getId());
    }
}
