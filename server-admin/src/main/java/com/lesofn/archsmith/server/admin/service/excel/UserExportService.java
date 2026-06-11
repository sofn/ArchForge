package com.lesofn.archsmith.server.admin.service.excel;

import com.lesofn.archsmith.common.utils.excel.FastExcelUtil;
import com.lesofn.archsmith.user.domain.SysUser;
import com.lesofn.archsmith.user.service.SysUserService;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Exports admin users as a FastExcel-generated xlsx workbook.
 *
 * @author sofn
 */
@Service
@RequiredArgsConstructor
public class UserExportService {

    private final SysUserService userService;

    /** Writes a single-sheet workbook of users to the given output stream. */
    public void exportTo(OutputStream out) throws IOException {
        List<SysUser> users = userService.findAll();
        List<String> headers =
                List.of("ID", "Username", "Nickname", "Email", "Phone", "Sex", "Status");
        List<List<Object>> rows = new ArrayList<>(users.size());
        for (SysUser u : users) {
            rows.add(
                    List.of(
                            u.getUserId() == null ? "" : u.getUserId(),
                            nullSafe(u.getUsername()),
                            nullSafe(u.getNickname()),
                            nullSafe(u.getEmail()),
                            nullSafe(u.getPhoneNumber()),
                            u.getSex() == null ? "" : u.getSex().name(),
                            u.getStatus() == null ? "" : u.getStatus()));
        }
        FastExcelUtil.write(out, "Users", headers, rows);
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
