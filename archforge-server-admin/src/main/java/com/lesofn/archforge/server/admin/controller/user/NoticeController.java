package com.lesofn.archforge.server.admin.controller.user;

import com.lesofn.archforge.server.admin.controller.ControllerHelper;
import com.lesofn.archforge.infrastructure.annotation.Log;
import com.lesofn.archforge.server.admin.dto.AdminPageResponse;
import com.lesofn.archforge.server.admin.dto.BasePageRequest;
import com.lesofn.archforge.server.admin.dto.request.NoticeCreateRequest;
import com.lesofn.archforge.server.admin.dto.request.NoticeDeleteRequest;
import com.lesofn.archforge.server.admin.dto.request.NoticeUpdateRequest;
import com.lesofn.archforge.server.admin.dto.response.NoticeResponse;
import com.lesofn.archforge.user.api.domain.SysNotice;
import com.lesofn.archforge.user.api.service.SysNoticeService;
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

@Tag(name = "通知公告")
@SaCheckLogin(type = StpAdminUtil.TYPE)
@SaCheckRole(value = "ADMIN", type = StpAdminUtil.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/notice")
public class NoticeController {

    private final SysNoticeService noticeService;

    @Operation(summary = "获取通知公告列表")
    @PostMapping
    public AdminPageResponse<NoticeResponse> getNoticeList(@RequestBody BasePageRequest request) {
        int currentPage = request.getCurrentPage() != null ? request.getCurrentPage() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<SysNotice> page = noticeService.findAll(pageable);
        List<NoticeResponse> list = page.getContent().stream()
                .filter(n -> !Boolean.TRUE.equals(n.getDeleted()))
                .map(n -> new NoticeResponse(n.getNoticeId(), n.getNoticeTitle(), n.getNoticeType(), n.getNoticeContent(), n
                        .getStatus(), n.getRemark(), ControllerHelper.toEpochMilli(n.getCreateTime())))
                .collect(Collectors.toList());
        return AdminPageResponse.of(list, page.getTotalElements(), pageSize, currentPage);
    }

    @Log
    @Operation(summary = "创建通知公告")
    @SaCheckPermission(value = "system:notice:add", type = StpAdminUtil.TYPE)
    @PostMapping("/create")
    public Long createNotice(@RequestBody @Valid NoticeCreateRequest request) {
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle(request.getNoticeTitle());
        notice.setNoticeType(request.getNoticeType() != null ? request.getNoticeType() : 1);
        notice.setNoticeContent(request.getNoticeContent() != null ? request.getNoticeContent() : "");
        notice.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        notice.setRemark(request.getRemark() != null ? request.getRemark() : "");
        SysNotice saved = noticeService.create(notice);
        return saved.getNoticeId();
    }

    @Log
    @Operation(summary = "更新通知公告")
    @SaCheckPermission(value = "system:notice:edit", type = StpAdminUtil.TYPE)
    @PutMapping("/update")
    public Boolean updateNotice(@RequestBody @Valid NoticeUpdateRequest request) {
        Optional<SysNotice> opt = noticeService.findById(request.getId());
        if (opt.isEmpty()) {
            return false;
        }
        SysNotice notice = opt.get();
        if (request.getNoticeTitle() != null)
            notice.setNoticeTitle(request.getNoticeTitle());
        if (request.getNoticeType() != null)
            notice.setNoticeType(request.getNoticeType());
        if (request.getNoticeContent() != null)
            notice.setNoticeContent(request.getNoticeContent());
        if (request.getStatus() != null)
            notice.setStatus(request.getStatus());
        if (request.getRemark() != null)
            notice.setRemark(request.getRemark());
        noticeService.update(notice);
        return true;
    }

    @Log
    @Operation(summary = "删除通知公告")
    @SaCheckPermission(value = "system:notice:remove", type = StpAdminUtil.TYPE)
    @PostMapping("/delete")
    public Boolean deleteNotice(@RequestBody @Valid NoticeDeleteRequest request) {
        noticeService.deleteById(request.getId());
        return true;
    }
}
