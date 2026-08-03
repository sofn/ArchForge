package com.lesofn.archforge.server.admin.dto.response;

import lombok.Data;

/**
 * 元表格代码生成响应。
 */
@Data
public class MetaTableGenerateResponse {

    /** 后端生成目录绝对路径 */
    private String backendDir;

    /** 前端生成目录绝对路径 */
    private String frontendDir;

    /** 生成文件数量 */
    private int files;
}
