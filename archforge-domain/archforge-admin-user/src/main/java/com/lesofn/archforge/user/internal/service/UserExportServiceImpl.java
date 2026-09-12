package com.lesofn.archforge.user.internal.service;

import com.lesofn.archforge.common.utils.excel.FastExcelUtil;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.api.service.SysUserService;
import com.lesofn.archforge.user.api.service.UserExportService;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserExportServiceImpl implements UserExportService {

    private final SysUserService userService;

    @Override
    public void exportTo(OutputStream out) throws IOException {
        List<SysUser> users = userService.findAll();
        List<String> headers = List.of("ID", "Username", "Nickname", "Email", "Phone", "Sex", "Status");
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
