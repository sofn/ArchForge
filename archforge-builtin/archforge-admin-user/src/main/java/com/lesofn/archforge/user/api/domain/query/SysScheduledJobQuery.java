package com.lesofn.archforge.user.api.domain.query;

import com.lesofn.archforge.common.annotation.Query;
import lombok.Data;
import org.jspecify.annotations.Nullable;

/**
 * Declarative criteria for {@code sys_scheduled_job} queries. Resolved into a JPA Specification by
 * {@code QueryHelp}.
 *
 * @author sofn
 */
@Data
public class SysScheduledJobQuery {

    @Query(type = Query.Type.INNER_LIKE)
    private @Nullable String jobName;

    @Query
    private @Nullable String jobGroup;

    @Query
    private @Nullable Short status;

    @Query
    private @Nullable Boolean deleted;
}
