package com.lesofn.archforge.server.admin.dto.response;

import lombok.AllArgsConstructor;
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
public class FileResponse {

    private Long id;

    private String originalName;

    private String storageName;

    private String storagePath;

    private Long fileSize;

    private String contentType;

    private String extension;

    private String storageType;

    private Long createTime;
}
