package com.lesofn.appforge.infrastructure.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import lombok.extern.slf4j.Slf4j;

/**
 * 本地文件存储实现
 *
 * @author sofn
 */
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    private final Path basePath;

    public LocalFileStorageService(String baseDir) {
        this.basePath = Paths.get(baseDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.basePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + baseDir, e);
        }
    }

    @Override
    public String upload(String path, InputStream inputStream, String contentType, long size) {
        try {
            Path targetPath = basePath.resolve(path).normalize();
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return path;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + path, e);
        }
    }

    @Override
    public InputStream download(String path) {
        try {
            Path filePath = basePath.resolve(path).normalize();
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + path, e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            Path filePath = basePath.resolve(path).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", path, e);
        }
    }

    @Override
    public boolean exists(String path) {
        Path filePath = basePath.resolve(path).normalize();
        return Files.exists(filePath);
    }
}
