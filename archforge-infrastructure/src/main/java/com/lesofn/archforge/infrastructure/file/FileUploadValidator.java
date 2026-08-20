package com.lesofn.archforge.infrastructure.file;

import com.lesofn.archforge.common.error.SystemErrorCode;
import com.lesofn.archforge.common.error.system.SystemException;
import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

public final class FileUploadValidator {

    private static final Set<String> SCRIPTABLE_EXTENSIONS = Set.of("svg", "html", "htm", "xhtml", "xml", "js");
    private static final Set<String> SCRIPTABLE_MIME_PREFIXES = Set.of("image/svg", "text/html", "application/xhtml");
    private static final Set<String> IMAGE_ONLY_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

    private FileUploadValidator() {
    }

    public static void validate(MultipartFile file, ArchForgeProperties.FileStorage fileStorage) {
        validate(file, fileStorage, false);
    }

    public static void validateImage(MultipartFile file, ArchForgeProperties.FileStorage fileStorage) {
        validate(file, fileStorage, true);
    }

    public static void validate(MultipartFile file, ArchForgeProperties.FileStorage fileStorage, boolean imageOnly) {
        if (file == null || file.isEmpty()) {
            throw new SystemException(SystemErrorCode.E_FILE_TYPE_NOT_ALLOWED);
        }
        long maxFileSize = fileStorage.getMaxFileSize();
        if (file.getSize() > maxFileSize) {
            throw new SystemException(SystemErrorCode.E_FILE_SIZE_EXCEEDED);
        }

        String extension = extension(file.getOriginalFilename());
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (SCRIPTABLE_EXTENSIONS.contains(extension) || isScriptableMime(contentType)) {
            throw new SystemException(SystemErrorCode.E_FILE_TYPE_NOT_ALLOWED);
        }

        List<String> allowedExtensions = fileStorage.getAllowedExtensions();
        if (allowedExtensions != null && !allowedExtensions.isEmpty()) {
            boolean allowed = allowedExtensions.stream().anyMatch(ext -> ext.equalsIgnoreCase(extension));
            if (!allowed) {
                throw new SystemException(SystemErrorCode.E_FILE_TYPE_NOT_ALLOWED);
            }
        }
        if (imageOnly && !IMAGE_ONLY_EXTENSIONS.contains(extension)) {
            throw new SystemException(SystemErrorCode.E_FILE_TYPE_NOT_ALLOWED);
        }
        if (imageOnly && !contentType.startsWith("image/")) {
            throw new SystemException(SystemErrorCode.E_FILE_TYPE_NOT_ALLOWED);
        }

        List<String> blockedMimeTypes = fileStorage.getBlockedMimeTypes();
        if (!contentType.isEmpty() && blockedMimeTypes != null) {
            boolean blocked = blockedMimeTypes.stream().anyMatch(mime -> mime.equalsIgnoreCase(contentType));
            if (blocked) {
                throw new SystemException(SystemErrorCode.E_FILE_TYPE_NOT_ALLOWED);
            }
        }
    }

    public static String extension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot >= 0 && dot < filename.length() - 1
                ? filename.substring(dot + 1).toLowerCase(Locale.ROOT)
                : "";
    }

    private static boolean isScriptableMime(String contentType) {
        return SCRIPTABLE_MIME_PREFIXES.stream().anyMatch(contentType::startsWith);
    }
}
