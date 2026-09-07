package com.lesofn.archforge.server.admin.dto.scheduler;

import com.lesofn.archforge.server.admin.dto.BasePageRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 执行日志查询请求 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerLogListRequest extends BasePageRequest {

    private Long jobId;

    /** 每页大小（默认 20） */
    private Integer pageSize = 20;
}
