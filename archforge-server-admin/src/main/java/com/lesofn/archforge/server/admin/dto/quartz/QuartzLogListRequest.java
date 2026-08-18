package com.lesofn.archforge.server.admin.dto.quartz;

import com.lesofn.archforge.server.admin.dto.BasePageRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Quartz 任务日志查询请求
 *
 * @author lesofn
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class QuartzLogListRequest extends BasePageRequest {

    private Long jobId;

    /** 每页大小（默认 20） */
    private Integer pageSize = 20;
}
