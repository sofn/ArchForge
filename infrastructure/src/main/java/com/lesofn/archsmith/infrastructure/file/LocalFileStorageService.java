package com.lesofn.archsmith.infrastructure.file;

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

    /**
     * 将用户提供的相对路径解析为安全的绝对路径，确保不会逃逸出 basePath。
     *
     * @throws IllegalArgumentException 如果路径包含路径遍历序列
     */
    private Path safePath(String relativePath) {
        Path resolved = basePath.resolve(relativePath).normalize();
        if (!resolved.startsWith(basePath)) {
            throw new IllegalArgumentException("Path traversal detected: " + relativePath);
        }
        return resolved;
    }

    @Override
    public String upload(String path, InputStream inputStream, String contentType, long size) {
        try {
            Path targetPath = safePath(path);
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
            Path filePath = safePath(path);
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + path, e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            Path filePath = safePath(path);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", path, e);
        }
    }

    @Override
    public boolean exists(String path) {
        Path filePath = safePath(path);
        return Files.exists(filePath);
    }
}
