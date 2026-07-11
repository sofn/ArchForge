package com.lesofn.archforge.server.admin.controller.admin;

import com.lesofn.archforge.infrastructure.annotation.Log;
import com.lesofn.archforge.server.admin.dto.AdminPageResult;
import com.lesofn.archforge.server.admin.dto.request.NoticeCreateRequest;
import com.lesofn.archforge.server.admin.dto.request.NoticeDeleteRequest;
import com.lesofn.archforge.server.admin.dto.request.NoticeUpdateRequest;
import com.lesofn.archforge.user.domain.SysNotice;
import com.lesofn.archforge.user.service.SysNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "通知公告")
@PreAuthorize("hasRole('ADMIN')")
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class AdminNoticeController {

    private final SysNoticeService noticeService;

    @Operation(summary = "获取通知公告列表")
    @PostMapping("/notice")
    public AdminPageResult<Map<String, Object>> getNoticeList(@RequestBody Map<String, Object> request) {
        int currentPage = AdminControllerHelper.getInt(request, "currentPage", 1);
        int pageSize = AdminControllerHelper.getInt(request, "pageSize", 10);
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<SysNotice> page = noticeService.findAll(pageable);
        List<Map<String, Object>> list = page.getContent().stream()
                .filter(n -> !Boolean.TRUE.equals(n.getDeleted()))
                .map(n -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", n.getNoticeId());
                    m.put("noticeTitle", n.getNoticeTitle());
                    m.put("noticeType", n.getNoticeType());
                    m.put("noticeContent", n.getNoticeContent());
                    m.put("status", n.getStatus());
                    m.put("remark", n.getRemark());
                    m.put("createTime", AdminControllerHelper.toEpochMilli(n.getCreateTime()));
                    return m;
                })
                .collect(Collectors.toList());
        return AdminPageResult.of(list, page.getTotalElements(), pageSize, currentPage);
    }

    @Log
    @Operation(summary = "创建通知公告")
    @PostMapping("/notice/create")
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
    @PutMapping("/notice/update")
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
    @PostMapping("/notice/delete")
    public Boolean deleteNotice(@RequestBody @Valid NoticeDeleteRequest request) {
        noticeService.deleteById(request.getId());
        return true;
    }
}
