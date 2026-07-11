package com.lesofn.archforge.server.admin.dto.request;

import com.lesofn.archforge.server.admin.dto.PageQuery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 文件列表查询请求
 *
 * @author lesofn
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class FileListRequest extends PageQuery {

    /** 原始文件名 */
    private String originalName;

    /** 存储类型 */
    private String storageType;
}
