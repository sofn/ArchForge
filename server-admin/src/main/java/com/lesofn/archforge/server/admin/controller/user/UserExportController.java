package com.lesofn.archforge.server.admin.controller.user;

import com.lesofn.archforge.server.admin.service.excel.UserExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Streams a FastExcel-generated xlsx of admin users.
 *
 * @author sofn
 */
@Slf4j
@Tag(name = "用户导出", description = "FastExcel 用户导出接口")
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/user")
public class UserExportController {

    private final UserExportService userExportService;

    @Operation(summary = "导出用户列表为 xlsx")
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=users.xlsx");
        userExportService.exportTo(response.getOutputStream());
    }
}
