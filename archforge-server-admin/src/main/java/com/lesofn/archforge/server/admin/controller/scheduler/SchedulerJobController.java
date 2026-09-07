package com.lesofn.archforge.server.admin.controller.scheduler;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import com.lesofn.archforge.infrastructure.annotation.Log;
import com.lesofn.archforge.server.admin.dto.AdminPageResponse;
import com.lesofn.archforge.server.admin.dto.scheduler.CronValidateRequest;
import com.lesofn.archforge.server.admin.dto.scheduler.SchedulerJobListRequest;
import com.lesofn.archforge.server.admin.dto.scheduler.SchedulerJobQueryRequest;
import com.lesofn.archforge.server.admin.dto.scheduler.SchedulerJobResponse;
import com.lesofn.archforge.server.admin.dto.scheduler.SchedulerJobUpsertRequest;
import com.lesofn.archforge.server.admin.dto.scheduler.SchedulerLogListRequest;
import com.lesofn.archforge.server.admin.dto.scheduler.SchedulerLogResponse;
import com.lesofn.archforge.server.admin.service.scheduler.ScheduledJobService;
import com.lesofn.archforge.user.api.domain.SysJobLog;
import com.lesofn.archforge.user.api.domain.SysScheduledJob;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin API for the db-scheduler-backed job platform (URL space {@code /quartz} and permission
 * codes {@code monitor:job:*} are contract-stable with the former Quartz API — the admin frontend
 * and ArchForgeSpec depend on them).
 *
 * <p>
 * Execution is reflective: a job row names a Spring bean and method (optionally a JSON array of
 * primitive arguments), invoked by {@code ReflectionJobHandler} on a db-scheduler worker thread
 * with a per-run audit log in {@code sys_job_log}.
 *
 * @author sofn
 */
@Tag(name = "定时任务管理", description = "db-scheduler 反射调度任务管理")
@SaCheckLogin(type = StpAdminUtil.TYPE)
@SaCheckRole(value = "ADMIN", type = StpAdminUtil.TYPE)
@RestController
@RequestMapping("/quartz")
@RequiredArgsConstructor
public class SchedulerJobController {

    private final ScheduledJobService jobService;

    @Operation(summary = "查询定时任务列表")
    @SaCheckPermission(value = "monitor:job:list", type = StpAdminUtil.TYPE)
    @GetMapping
    public AdminPageResponse<SchedulerJobResponse> list(SchedulerJobListRequest query) {
        int currentPage = query.getCurrentPage() != null && query.getCurrentPage() > 0
                ? query.getCurrentPage()
                : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 10;
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        SchedulerJobQueryRequest criteria = new SchedulerJobQueryRequest();
        criteria.setJobName(query.getJobName());
        criteria.setJobGroup(query.getJobGroup());
        criteria.setStatus(query.getStatus());
        Page<SysScheduledJob> page = jobService.page(criteria, pageable);
        return AdminPageResponse.of(
                page.getContent().stream().map(SchedulerJobResponse::from).toList(),
                page.getTotalElements(),
                pageSize,
                currentPage);
    }

    @Log
    @Operation(summary = "新增定时任务")
    @SaCheckPermission(value = "monitor:job:add", type = StpAdminUtil.TYPE)
    @PostMapping("/add")
    public Long add(@RequestBody SchedulerJobUpsertRequest req) {
        return jobService.add(toEntity(new SysScheduledJob(), req));
    }

    @Log
    @Operation(summary = "更新定时任务")
    @SaCheckPermission(value = "monitor:job:edit", type = StpAdminUtil.TYPE)
    @PutMapping("/update/{id}")
    public void update(@PathVariable Long id, @RequestBody SchedulerJobUpsertRequest req) {
        jobService.update(id, toEntity(new SysScheduledJob(), req));
    }

    @Log
    @Operation(summary = "删除定时任务")
    @SaCheckPermission(value = "monitor:job:remove", type = StpAdminUtil.TYPE)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        jobService.delete(id);
    }

    @Log
    @Operation(summary = "暂停定时任务")
    @SaCheckPermission(value = "monitor:job:edit", type = StpAdminUtil.TYPE)
    @PostMapping("/pause/{id}")
    public void pause(@PathVariable Long id) {
        jobService.pause(id);
    }

    @Log
    @Operation(summary = "恢复定时任务")
    @SaCheckPermission(value = "monitor:job:edit", type = StpAdminUtil.TYPE)
    @PostMapping("/resume/{id}")
    public void resume(@PathVariable Long id) {
        jobService.resume(id);
    }

    @Log
    @Operation(summary = "立即执行一次")
    @SaCheckPermission(value = "monitor:job:edit", type = StpAdminUtil.TYPE)
    @PostMapping("/run/{id}")
    public void run(@PathVariable Long id) {
        jobService.runOnce(id);
    }

    @Operation(summary = "查询任务执行日志")
    @SaCheckPermission(value = "monitor:job:list", type = StpAdminUtil.TYPE)
    @GetMapping("/log")
    public AdminPageResponse<SchedulerLogResponse> logList(SchedulerLogListRequest body) {
        Long jobId = body.getJobId();
        int currentPage = body.getCurrentPage() != null && body.getCurrentPage() > 0
                ? body.getCurrentPage()
                : 1;
        int pageSize = body.getPageSize() != null && body.getPageSize() > 0
                ? body.getPageSize()
                : 20;
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<SysJobLog> page = jobService.logPage(jobId, pageable);
        return AdminPageResponse.of(
                page.getContent().stream().map(SchedulerLogResponse::from).toList(),
                page.getTotalElements(),
                pageSize,
                currentPage);
    }

    @Operation(summary = "校验 cron 表达式")
    @SaCheckPermission(value = "monitor:job:list", type = StpAdminUtil.TYPE)
    @PostMapping("/validate-cron")
    public boolean validateCron(@RequestBody CronValidateRequest body) {
        return jobService.validateCron(body.getCron());
    }

    private static SysScheduledJob toEntity(SysScheduledJob target, SchedulerJobUpsertRequest req) {
        target.setJobName(req.getJobName());
        target.setJobGroup(req.getJobGroup());
        target.setDescription(req.getDescription());
        target.setBeanName(req.getBeanName());
        target.setMethodName(req.getMethodName());
        target.setMethodParams(req.getMethodParams());
        target.setCron(req.getCron());
        target.setMisfirePolicy(req.getMisfirePolicy());
        target.setConcurrent(req.getConcurrent());
        return target;
    }
}
