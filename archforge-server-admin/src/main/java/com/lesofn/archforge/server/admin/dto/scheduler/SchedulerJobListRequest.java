package com.lesofn.archforge.server.admin.dto.scheduler;

import com.lesofn.archforge.server.admin.dto.BasePageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 列表查询请求 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SchedulerJobListRequest extends BasePageRequest {

    /** 任务名 */
    private String jobName;

    /** 任务分组 */
    private String jobGroup;

    /** 状态（0=运行 1=暂停） */
    private Short status;
}
