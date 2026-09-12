package com.lesofn.archforge.server.admin.dto.response;

import lombok.Data;
import org.jspecify.annotations.Nullable;

/**
 * 元表格代码生成响应。
 */
@Data
@SuppressWarnings("NullAway.Init")
public class MetaTableGenerateResponse {

    /** 后端生成目录绝对路径 */
    private @Nullable String backendDir;

    /** 前端生成目录绝对路径 */
    private @Nullable String frontendDir;

    /** 生成文件数量 */
    private @Nullable int files;
}
