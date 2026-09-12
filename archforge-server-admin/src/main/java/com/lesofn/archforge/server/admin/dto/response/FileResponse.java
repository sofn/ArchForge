package com.lesofn.archforge.server.admin.dto.response;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件信息响应
 *
 * @author lesofn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("NullAway.Init")
public class FileResponse {

    private @Nullable Long id;

    private @Nullable String originalName;

    private @Nullable String storageName;

    private @Nullable String storagePath;

    private @Nullable Long fileSize;

    private @Nullable String contentType;

    private @Nullable String extension;

    private @Nullable String storageType;

    private @Nullable Long createTime;
}
