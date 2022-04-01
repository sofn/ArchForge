package com.lesofn.appengine.common.error.response;

import lombok.Data;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 18:37
 */
@Data
public class CustomResult<T> {

    /**
     * 业务数据
     */
    private T data;

}
