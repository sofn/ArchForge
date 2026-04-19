package com.lesofn.archsmith.server.admin.controller;

import com.lesofn.archsmith.common.error.system.SystemException;
import com.lesofn.archsmith.common.errors.SystemErrorCode;
import com.lesofn.archsmith.infrastructure.config.ArchSmithConfig;
import com.lesofn.archsmith.infrastructure.file.FileStorageService;
import com.lesofn.archsmith.user.dao.SysFileRepository;
import com.lesofn.archsmith.user.domain.SysFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传下载控制器
 *
 * @author sofn
 */
@Slf4j
@Tag(name = "文件管理", description = "文件上传、下载、删除接口")
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;
    private final SysFileRepository fileRepository;
    private final ArchSmithConfig appForgeConfig;

    @Operation(summary = "上传文件")
    @PostMapping("/file/upload")
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile file) {
        validateUploadFile(file);
        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        String storageName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String storagePath = datePath + "/" + storageName;

        try (InputStream inputStream = file.getInputStream()) {
            fileStorageService.upload(
                    storagePath, inputStream, file.getContentType(), file.getSize());
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }

        SysFile sysFile = new SysFile();
        sysFile.setOriginalName(originalName);
        sysFile.setStorageName(storageName);
        sysFile.setStoragePath(storagePath);
        sysFile.setFileSize(file.getSize());
        sysFile.setContentType(file.getContentType());
        sysFile.setExtension(extension);
        sysFile.setStorageType(appForgeConfig.getFileStorage().getType());
        SysFile saved = fileRepository.save(sysFile);

        Map<String, Object> result = new HashMap<>();
        result.put("fileId", saved.getFileId());
        result.put("originalName", originalName);
        result.put("url", "/file/download/" + saved.getFileId());
        result.put("fileSize", file.getSize());
        return result;
    }

    @Operation(summary = "上传图片（头像等）")
    @PostMapping("/file/upload-image")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) {
        return uploadFile(file);
    }

    @Operation(summary = "下载文件")
    @GetMapping("/file/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        Optional<SysFile> optFile = fileRepository.findById(fileId);
        if (optFile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        SysFile sysFile = optFile.get();
        InputStream inputStream = fileStorageService.download(sysFile.getStoragePath());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(sysFile.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + sysFile.getOriginalName() + "\"")
                .body(new InputStreamResource(inputStream));
    }

    @Operation(summary = "删除文件")
    @PostMapping("/file/delete")
    public Boolean deleteFile(@RequestBody Map<String, Object> data) {
        Long fileId = Long.valueOf(data.get("id").toString());
        Optional<SysFile> optFile = fileRepository.findById(fileId);
        if (optFile.isEmpty()) {
            return false;
        }
        SysFile sysFile = optFile.get();
        fileStorageService.delete(sysFile.getStoragePath());
        fileRepository.deleteById(fileId);
        return true;
    }

    private String getExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot >= 0 && dot < filename.length() - 1
                ? filename.substring(dot + 1).toLowerCase()
                : "";
    }

    private void validateUploadFile(MultipartFile file) {
        ArchSmithConfig.FileStorage fileStorage = appForgeConfig.getFileStorage();

        long maxFileSize = fileStorage.getMaxFileSize();
        if (file.getSize() > maxFileSize) {
            throw new SystemException(SystemErrorCode.E_FILE_SIZE_EXCEEDED);
        }

        String extension = getExtension(file.getOriginalFilename());
        List<String> allowedExtensions = fileStorage.getAllowedExtensions();
        if (allowedExtensions != null && !allowedExtensions.isEmpty()) {
            boolean allowed =
                    allowedExtensions.stream().anyMatch(ext -> ext.equalsIgnoreCase(extension));
            if (!allowed) {
                throw new SystemException(SystemErrorCode.E_FILE_TYPE_NOT_ALLOWED);
            }
        }

        String contentType = file.getContentType();
        List<String> blockedMimeTypes = fileStorage.getBlockedMimeTypes();
        if (contentType != null && blockedMimeTypes != null) {
            boolean blocked =
                    blockedMimeTypes.stream().anyMatch(mime -> mime.equalsIgnoreCase(contentType));
            if (blocked) {
                throw new SystemException(SystemErrorCode.E_FILE_TYPE_NOT_ALLOWED);
            }
        }
    }
}
