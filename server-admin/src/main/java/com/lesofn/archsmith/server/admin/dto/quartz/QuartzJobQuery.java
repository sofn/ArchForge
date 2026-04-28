package com.lesofn.archsmith.server.admin.dto.quartz;

import lombok.Data;

/** Query payload for {@code POST /quartz/list}. */
@Data
public class QuartzJobQuery {
    private String jobName;
    private Short status;
    private Integer currentPage;
    private Integer pageSize;
}
