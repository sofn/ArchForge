package com.lesofn.archsmith.server.admin.dto.user;

import com.lesofn.archsmith.common.annotation.Query;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * Declarative criteria DTO for {@code POST /user}. Resolved into a JPA Specification by {@code
 * QueryHelp}.
 *
 * @author sofn
 */
@Data
public class SysUserQueryCriteria {

    /** Multi-field LIKE across {@code username,nickname,email}. */
    @Query(blurry = "username,nickname,email")
    private String blurry;

    @Query(type = Query.Type.INNER_LIKE)
    private String username;

    @Query(type = Query.Type.INNER_LIKE)
    private String email;

    @Query(type = Query.Type.INNER_LIKE)
    private String phoneNumber;

    @Query private Integer status;

    @Query private Long deptId;

    @Query private Boolean deleted;

    /** Two-element list {@code [start, end]} — ignored if size != 2. */
    @Query(type = Query.Type.BETWEEN)
    private List<LocalDateTime> createTime;
}
