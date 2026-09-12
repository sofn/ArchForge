package com.lesofn.archforge.server.admin.dto.response;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应
 *
 * @author lesofn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("NullAway.Init")
public class UploadFileResponse {

    private @Nullable Long fileId;

    private @Nullable String originalName;

    private @Nullable String url;

    private @Nullable Long fileSize;
}
