package com.lesofn.archforge.server.admin.controller.blog;

import com.lesofn.archforge.infrastructure.file.FileStorageService;
import com.lesofn.archforge.user.api.domain.SysFile;
import com.lesofn.archforge.user.api.service.SysFileService;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/blog/file")
@SaCheckLogin(type = StpAdminUtil.TYPE)
@SaCheckRole(value = "ADMIN", type = StpAdminUtil.TYPE)
@RequiredArgsConstructor
public class BlogFileController {

    private final FileStorageService fileStorageService;
    private final SysFileService sysFileService;

    @Value("${arch-forge.web.public-url:http://localhost:8081}")
    private String webPublicUrl;

    @SaCheckPermission(value = "blog:file:add", type = StpAdminUtil.TYPE)
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadMarkdownImage(
            @RequestParam("file[]") MultipartFile[] files) throws IOException {
        Map<String, String> succMap = new HashMap<>();
        List<String> errFiles = new java.util.ArrayList<>();

        if (files != null) {
            for (MultipartFile file : files) {
                String originalName = file.getOriginalFilename();
                if (!StringUtils.hasText(originalName)) {
                    errFiles.add("unknown");
                    continue;
                }
                String extension = StringUtils.getFilenameExtension(originalName);
                if (!isImage(extension)) {
                    errFiles.add(originalName);
                    continue;
                }
                String storagePath = "blog/" + LocalDate.now().getYear() + "/" + LocalDate.now().getMonthValue() + "/" + UUID
                        .randomUUID().toString().replace("-", "") + "." + extension;
                try (InputStream inputStream = file.getInputStream()) {
                    fileStorageService.upload(storagePath, inputStream, file.getContentType(), file.getSize());
                }
                SysFile sysFile = new SysFile()
                        .setOriginalName(originalName)
                        .setStorageName(StringUtils.getFilename(storagePath))
                        .setStoragePath(storagePath)
                        .setFileSize(file.getSize())
                        .setContentType(file.getContentType())
                        .setExtension(extension)
                        .setStorageType("local")
                        .setPublicVisible(true);
                SysFile saved = sysFileService.create(sysFile);
                succMap.put(originalName, webPublicUrl + "/web/file/" + saved.getFileId());
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("errFiles", errFiles);
        data.put("succMap", succMap);

        Map<String, Object> result = new HashMap<>();
        result.put("msg", "");
        result.put("code", 0);
        result.put("data", data);
        return ResponseEntity.ok(result);
    }

    private boolean isImage(String extension) {
        if (!StringUtils.hasText(extension)) {
            return false;
        }
        String lower = extension.toLowerCase();
        return lower.equals("jpg") || lower.equals("jpeg") || lower.equals("png") || lower.equals("gif") || lower.equals(
                "webp") || lower.equals("svg");
    }
}
