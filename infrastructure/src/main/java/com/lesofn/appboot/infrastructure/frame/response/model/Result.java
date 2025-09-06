package com.lesofn.appboot.infrastructure.frame.response.model;

import lombok.Data;

/**
 * for customer special success response
 *
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 18:37
 */
@Data
public class Result<T> {

    /**
     * 业务数据
     */
    private T data;

}
