package com.lesofn.archforge.server.admin.dto.request;

import lombok.Data;

/**
 * 元表格代码生成请求。
 */
@Data
public class MetaTableGenerateRequest {

    /** 后端输出目录，相对项目根目录，默认 example/<tableCode> */
    private String backendDir;

    /** 前端输出目录，相对项目根目录，默认 src/views/<tableCode> */
    private String frontendDir;

    /** 接口基础路径，默认 /generated/<tableCode> */
    private String basePath;

    /** 是否覆盖已存在的生成目录 */
    private Boolean overwrite = false;
}
