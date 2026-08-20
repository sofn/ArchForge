package com.lesofn.archforge.server.admin.controller.file;

import com.lesofn.archforge.common.error.system.SystemException;
import com.lesofn.archforge.infrastructure.annotation.Log;
import com.lesofn.archforge.infrastructure.annotation.RepeatSubmit;
import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import com.lesofn.archforge.infrastructure.file.FileStorageService;
import com.lesofn.archforge.infrastructure.file.FileUploadValidator;
import java.nio.charset.StandardCharsets;
import com.lesofn.archforge.server.admin.controller.ControllerHelper;
import com.lesofn.archforge.server.admin.dto.AdminPageResponse;
import com.lesofn.archforge.server.admin.dto.request.FileListRequest;
import com.lesofn.archforge.server.admin.dto.response.FileResponse;
import com.lesofn.archforge.server.admin.dto.response.UploadFileResponse;
import com.lesofn.archforge.user.api.domain.SysFile;
import com.lesofn.archforge.user.api.service.SysFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传下载控制器
 *
 * @author sofn
 */
@Slf4j
@Tag(name = "文件管理", description = "文件上传、下载、删除接口")
@SaCheckLogin(type = StpAdminUtil.TYPE)
@SaCheckRole(value = "ADMIN", type = StpAdminUtil.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {

    private final FileStorageService fileStorageService;
    private final SysFileService fileService;
    private final ArchForgeProperties appForgeConfig;

    @Log
    @Operation(summary = "上传文件")
    @SaCheckPermission(value = "system:file:add", type = StpAdminUtil.TYPE)
    @com.lesofn.archforge.infrastructure.annotation.RateLimit(key = "file-upload", time = 60, maxCount = 20,
            limitType = com.lesofn.archforge.infrastructure.annotation.RateLimit.LimitType.IP)
    @PostMapping("/upload")
    @RepeatSubmit
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
        return store(file, false);
    }

    @Log
    @Operation(summary = "上传图片（头像等）")
    @SaCheckPermission(value = "system:file:add", type = StpAdminUtil.TYPE)
    @PostMapping("/upload-image")
    @RepeatSubmit
    public UploadFileResponse uploadImage(@RequestParam("file") MultipartFile file) {
        return store(file, true);
    }

    private UploadFileResponse store(MultipartFile file, boolean imageOnly) {
        if (imageOnly) {
            FileUploadValidator.validateImage(file, appForgeConfig.getFileStorage());
        } else {
            FileUploadValidator.validate(file, appForgeConfig.getFileStorage());
        }
        String originalName = file.getOriginalFilename();
        String extension = FileUploadValidator.extension(originalName);
        String storageName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String storagePath = datePath + "/" + storageName;

        try (InputStream inputStream = file.getInputStream()) {
            fileStorageService.upload(
                    storagePath, inputStream, file.getContentType(), file.getSize());
        } catch (Exception e) {
            throw new SystemException("文件上传失败", e);
        }

        SysFile sysFile = new SysFile();
        sysFile.setOriginalName(originalName);
        sysFile.setStorageName(storageName);
        sysFile.setStoragePath(storagePath);
        sysFile.setFileSize(file.getSize());
        sysFile.setContentType(file.getContentType());
        sysFile.setExtension(extension);
        sysFile.setStorageType(appForgeConfig.getFileStorage().getType());
        SysFile saved = fileService.create(sysFile);

        return new UploadFileResponse(saved.getFileId(), originalName, "/file/download/" + saved.getFileId(), file.getSize());
    }

    @Operation(summary = "获取文件列表")
    @GetMapping
    public AdminPageResponse<FileResponse> listFiles(FileListRequest params) {
        int currentPage = params.getCurrentPage() != null ? params.getCurrentPage() : 1;
        int pageSize = params.getPageSize() != null ? params.getPageSize() : 10;
        String originalName = params.getOriginalName() != null ? params.getOriginalName() : "";
        String storageType = params.getStorageType() != null ? params.getStorageType() : "";

        Pageable pageable = PageRequest.of(
                Math.max(0, currentPage - 1), pageSize, Sort.by(Sort.Direction.DESC, "createTime"));

        Page<SysFile> page = fileService.findFiles(originalName, storageType, pageable);
        List<FileResponse> list = new ArrayList<>();
        for (SysFile file : page.getContent()) {
            list.add(new FileResponse(file.getFileId(), file.getOriginalName(), file.getStorageName(), file
                    .getStoragePath(), file.getFileSize(), file.getContentType(), file.getExtension(), file
                            .getStorageType(), ControllerHelper.toEpochMilli(file.getCreateTime())));
        }

        return AdminPageResponse.of(list, page.getTotalElements(), page.getSize(), currentPage);
    }

    @Operation(summary = "下载文件")
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        SysFile sysFile = fileService.findById(fileId).orElse(null);
        if (sysFile == null) {
            return ResponseEntity.notFound().build();
        }
        InputStream inputStream = fileStorageService.download(sysFile.getStoragePath());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(sysFile.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        org.springframework.http.ContentDisposition.attachment()
                                .filename(sysFile.getOriginalName(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(new InputStreamResource(inputStream));
    }

    @Log
    @Operation(summary = "删除文件")
    @SaCheckPermission(value = "system:file:remove", type = StpAdminUtil.TYPE)
    @DeleteMapping("/{fileId}")
    public Boolean deleteFile(@PathVariable Long fileId) {
        SysFile sysFile = fileService.findById(fileId).orElse(null);
        if (sysFile == null) {
            return false;
        }
        fileStorageService.delete(sysFile.getStoragePath());
        fileService.deleteById(fileId);
        return true;
    }

}
