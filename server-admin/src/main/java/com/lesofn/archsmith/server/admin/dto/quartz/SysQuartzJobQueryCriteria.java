package com.lesofn.archsmith.server.admin.dto.quartz;

import com.lesofn.archsmith.common.annotation.Query;
import lombok.Data;

/**
 * Declarative criteria DTO for {@code POST /quartz/list}. Resolved into a JPA Specification by
 * {@code QueryHelp}.
 *
 * @author sofn
 */
@Data
public class SysQuartzJobQueryCriteria {

    @Query(type = Query.Type.INNER_LIKE)
    private String jobName;

    @Query private String jobGroup;

    @Query private Short status;

    @Query private Boolean deleted;
}
