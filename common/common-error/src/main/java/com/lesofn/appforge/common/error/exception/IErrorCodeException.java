package com.lesofn.appforge.common.error.exception;

import com.lesofn.appforge.common.error.api.ProjectModule;
import com.lesofn.appforge.common.error.manager.ErrorInfo;

/**
 * @author lishaofeng02
 * @version 1.0 Created at: 2022-03-24 15:35
 */
public interface IErrorCodeException {
    /**
     * 错误信息
     */
    ErrorInfo getErrorInfo();

    /**
     * 服务+模块信息
     */
    ProjectModule projectModule();
}
