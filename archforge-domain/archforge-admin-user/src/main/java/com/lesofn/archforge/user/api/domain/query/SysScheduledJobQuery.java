package com.lesofn.archforge.user.api.domain.query;

import com.lesofn.archforge.common.annotation.Query;
import lombok.Data;

/**
 * Declarative criteria for {@code sys_scheduled_job} queries. Resolved into a JPA Specification by
 * {@code QueryHelp}.
 *
 * @author sofn
 */
@Data
public class SysScheduledJobQuery {

    @Query(type = Query.Type.INNER_LIKE)
    private String jobName;

    @Query
    private String jobGroup;

    @Query
    private Short status;

    @Query
    private Boolean deleted;
}
