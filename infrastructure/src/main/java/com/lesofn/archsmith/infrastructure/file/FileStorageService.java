package com.lesofn.archsmith.infrastructure.file;

import java.io.InputStream;

/**
 * 文件存储服务接口
 *
 * @author sofn
 */
public interface FileStorageService {

    /**
     * 上传文件
     *
     * @param path 存储路径
     * @param inputStream 文件流
     * @param contentType 文件类型
     * @param size 文件大小
     * @return 文件访问路径
     */
    String upload(String path, InputStream inputStream, String contentType, long size);

    /**
     * 下载文件
     *
     * @param path 文件路径
     * @return 文件流
     */
    InputStream download(String path);

    /**
     * 删除文件
     *
     * @param path 文件路径
     */
    void delete(String path);

    /**
     * 检查文件是否存在
     *
     * @param path 文件路径
     * @return 是否存在
     */
    boolean exists(String path);
}
