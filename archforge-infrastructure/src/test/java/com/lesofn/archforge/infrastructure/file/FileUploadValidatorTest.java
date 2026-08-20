package com.lesofn.archforge.infrastructure.file;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.lesofn.archforge.common.error.system.SystemException;
import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class FileUploadValidatorTest {

    @Test
    void rejectsSvgAndHtmlEvenWhenDeclaredAsImage() {
        ArchForgeProperties.FileStorage storage = imageOnlyStorage();
        MockMultipartFile svg = new MockMultipartFile("file", "xss.svg", "image/svg+xml", "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>"
                .getBytes(StandardCharsets.UTF_8));
        assertThrows(SystemException.class, () -> FileUploadValidator.validate(svg, storage));

        MockMultipartFile html = new MockMultipartFile("file", "page.html", "text/html", "<script>alert(1)</script>".getBytes(
                StandardCharsets.UTF_8));
        assertThrows(SystemException.class, () -> FileUploadValidator.validate(html, storage));
    }

    @Test
    void acceptsBitmapPng() {
        ArchForgeProperties.FileStorage storage = imageOnlyStorage();
        byte[] png = new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0
        };
        MockMultipartFile file = new MockMultipartFile("file", "cover.png", "image/png", png);
        assertDoesNotThrow(() -> FileUploadValidator.validate(file, storage));
    }

    private static ArchForgeProperties.FileStorage imageOnlyStorage() {
        ArchForgeProperties.FileStorage storage = new ArchForgeProperties.FileStorage();
        storage.setMaxFileSize(1024 * 1024);
        storage.setAllowedExtensions(java.util.List.of("jpg", "jpeg", "png", "gif", "webp", "bmp"));
        return storage;
    }
}
