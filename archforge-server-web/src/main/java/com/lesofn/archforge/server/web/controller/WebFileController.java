package com.lesofn.archforge.server.web.controller;

import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import com.lesofn.archforge.infrastructure.file.FileStorageService;
import com.lesofn.archforge.server.web.context.WebUserContext;
import com.lesofn.archforge.server.web.dto.FileUploadResponse;
import com.lesofn.archforge.user.api.domain.SysFile;
import com.lesofn.archforge.user.api.service.SysFileService;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/web/file")
@RequiredArgsConstructor
public class WebFileController {

    private final SysFileService sysFileService;
    private final FileStorageService fileStorageService;
    private final ArchForgeProperties appForgeConfig;

    @Value("${arch-forge.web.public-url:http://localhost:8081}")
    private String webPublicUrl;

    @GetMapping("/{fileId}")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long fileId) throws IOException {
        Optional<SysFile> fileOpt = sysFileService.findById(fileId);
        if (fileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        SysFile sysFile = fileOpt.get();
        InputStream inputStream = fileStorageService.download(sysFile.getStoragePath());
        InputStreamResource resource = new InputStreamResource(inputStream);
        MediaType mediaType = sysFile.getContentType() != null
                ? MediaType.parseMediaType(sysFile.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + sysFile.getOriginalName() + "\"")
                .contentType(mediaType)
                .body(resource);
    }

    @PostMapping("/upload")
    public FileUploadResponse upload(@RequestParam("file") MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        String baseName = UUID.randomUUID().toString().replace("-", "");
        String storageName = extension.isEmpty() ? baseName : baseName + "." + extension;
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String storagePath = datePath + "/" + storageName;

        fileStorageService.upload(storagePath, file.getInputStream(), file.getContentType(), file.getSize());

        SysFile sysFile = new SysFile();
        sysFile.setOriginalName(originalName);
        sysFile.setStorageName(storageName);
        sysFile.setStoragePath(storagePath);
        sysFile.setFileSize(file.getSize());
        sysFile.setContentType(file.getContentType());
        sysFile.setExtension(extension);
        sysFile.setStorageType(appForgeConfig.getFileStorage().getType());
        SysFile saved = sysFileService.create(sysFile);

        String url = webPublicUrl + "/web/file/" + saved.getFileId();
        return FileUploadResponse.builder()
                .fileId(saved.getFileId())
                .url(url)
                .name(originalName)
                .build();
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
