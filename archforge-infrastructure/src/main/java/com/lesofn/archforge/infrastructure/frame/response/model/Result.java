package com.lesofn.archforge.infrastructure.frame.response.model;

import lombok.Data;
import org.jspecify.annotations.Nullable;

/**
 * for customer special success response
 *
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 18:37
 */
@Data
public class Result<T> {

    /** 业务数据 */
    private @Nullable T data;
}
