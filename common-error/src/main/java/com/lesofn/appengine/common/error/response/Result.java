package com.lesofn.appengine.common.error.response;

import com.lesofn.appengine.common.error.ErrorInfo;

/**
 * @author lishaofeng
 * @version 1.0 Created at: 2022-03-09 18:34
 */
public class Result<T> extends ErrorInfo {

    private T data;

    public Result(int code, String msg) {
        super(code, msg);
    }
}
