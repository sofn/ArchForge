package com.lesofn.archforge.infrastructure.file;

import java.io.InputStream;

/**
 * 可重新配置的文件存储服务包装类。
 *
 * <p>
 * 支持在启动时根据 sys_config 中的最新配置重新构建底层存储实现。
 *
 * @author sofn
 */
public class AdaptiveFileStorageService implements FileStorageService {

    private volatile FileStorageService delegate;

    public AdaptiveFileStorageService(FileStorageService delegate) {
        this.delegate = delegate;
    }

    public void setDelegate(FileStorageService delegate) { this.delegate = delegate; }

    @Override
    public String upload(String path, InputStream inputStream, String contentType, long size) {
        return delegate.upload(path, inputStream, contentType, size);
    }

    @Override
    public InputStream download(String path) {
        return delegate.download(path);
    }

    @Override
    public void delete(String path) {
        delegate.delete(path);
    }

    @Override
    public boolean exists(String path) {
        return delegate.exists(path);
    }
}
