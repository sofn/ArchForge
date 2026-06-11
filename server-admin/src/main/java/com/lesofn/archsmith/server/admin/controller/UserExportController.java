package com.lesofn.archsmith.server.admin.controller;

import com.lesofn.archsmith.server.admin.service.excel.UserExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Streams a FastExcel-generated xlsx of admin users.
 *
 * @author sofn
 */
@Slf4j
@Tag(name = "用户导出", description = "FastExcel 用户导出接口")
@RestController
@RequiredArgsConstructor
public class UserExportController {

    private final UserExportService userExportService;

    @Operation(summary = "导出用户列表为 xlsx")
    @GetMapping("/user/export")
    public void export(HttpServletResponse response) throws IOException {
        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=users.xlsx");
        userExportService.exportTo(response.getOutputStream());
    }
}
