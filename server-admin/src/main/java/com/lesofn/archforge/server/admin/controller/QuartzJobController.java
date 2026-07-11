package com.lesofn.archforge.server.admin.controller;

import com.lesofn.archforge.infrastructure.annotation.Log;
import com.lesofn.archforge.server.admin.dto.AdminPageResult;
import com.lesofn.archforge.server.admin.dto.quartz.CronValidateRequest;
import com.lesofn.archforge.server.admin.dto.quartz.QuartzJobQuery;
import com.lesofn.archforge.server.admin.dto.quartz.QuartzJobResponse;
import com.lesofn.archforge.server.admin.dto.quartz.QuartzJobUpsertRequest;
import com.lesofn.archforge.server.admin.dto.quartz.QuartzLogListRequest;
import com.lesofn.archforge.server.admin.dto.quartz.QuartzLogResponse;
import com.lesofn.archforge.server.admin.dto.quartz.SysQuartzJobQueryCriteria;
import com.lesofn.archforge.server.admin.service.quartz.QuartzJobService;
import com.lesofn.archforge.user.domain.SysQuartzJob;
import com.lesofn.archforge.user.domain.SysQuartzLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin REST surface for Quartz reflective jobs.
 *
 * @author sofn
 */
@Tag(name = "Quartz 调度", description = "Quartz 反射调度任务管理")
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/quartz")
@RequiredArgsConstructor
public class QuartzJobController {

    private final QuartzJobService quartzJobService;

    @Operation(summary = "查询 Quartz 任务列表")
    @PostMapping("/list")
    public AdminPageResult<QuartzJobResponse> list(@RequestBody QuartzJobQuery query) {
        int currentPage = query.getCurrentPage() != null && query.getCurrentPage() > 0
                ? query.getCurrentPage()
                : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 10;
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        SysQuartzJobQueryCriteria criteria = new SysQuartzJobQueryCriteria();
        criteria.setJobName(query.getJobName());
        criteria.setStatus(query.getStatus());
        Page<SysQuartzJob> page = quartzJobService.page(criteria, pageable);
        return AdminPageResult.of(
                page.getContent().stream().map(QuartzJobResponse::from).toList(),
                page.getTotalElements(),
                pageSize,
                currentPage);
    }

    @Log
    @Operation(summary = "新增 Quartz 任务")
    @PostMapping("/add")
    public Long add(@RequestBody QuartzJobUpsertRequest req) {
        return quartzJobService.add(toEntity(new SysQuartzJob(), req));
    }

    @Log
    @Operation(summary = "更新 Quartz 任务")
    @PutMapping("/update/{id}")
    public void update(@PathVariable Long id, @RequestBody QuartzJobUpsertRequest req) {
        quartzJobService.update(id, toEntity(new SysQuartzJob(), req));
    }

    @Log
    @Operation(summary = "删除 Quartz 任务")
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        quartzJobService.delete(id);
    }

    @Log
    @Operation(summary = "暂停 Quartz 任务")
    @PostMapping("/pause/{id}")
    public void pause(@PathVariable Long id) {
        quartzJobService.pause(id);
    }

    @Log
    @Operation(summary = "恢复 Quartz 任务")
    @PostMapping("/resume/{id}")
    public void resume(@PathVariable Long id) {
        quartzJobService.resume(id);
    }

    @Log
    @Operation(summary = "立即执行一次")
    @PostMapping("/run/{id}")
    public void run(@PathVariable Long id) {
        quartzJobService.runOnce(id);
    }

    @Operation(summary = "查询任务执行日志")
    @PostMapping("/log/list")
    public AdminPageResult<QuartzLogResponse> logList(@RequestBody QuartzLogListRequest body) {
        Long jobId = body.getJobId();
        int currentPage = body.getCurrentPage() != null && body.getCurrentPage() > 0
                ? body.getCurrentPage()
                : 1;
        int pageSize = body.getPageSize() != null && body.getPageSize() > 0
                ? body.getPageSize()
                : 20;
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<SysQuartzLog> page = quartzJobService.logPage(jobId, pageable);
        return AdminPageResult.of(
                page.getContent().stream().map(QuartzLogResponse::from).toList(),
                page.getTotalElements(),
                pageSize,
                currentPage);
    }

    @Operation(summary = "校验 cron 表达式")
    @PostMapping("/validate-cron")
    public boolean validateCron(@RequestBody CronValidateRequest body) {
        return quartzJobService.validateCron(body.getCron());
    }

    private static SysQuartzJob toEntity(SysQuartzJob target, QuartzJobUpsertRequest req) {
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
