package com.lesofn.appboot.demo.task.errors;

import com.lesofn.appboot.common.error.api.ErrorCode;
import com.lesofn.appboot.common.error.manager.ErrorManager;
import com.lesofn.appboot.common.errors.AppBootProjectModule;
import lombok.Getter;

/**
 * 基础错误码定义
 *
 * @author sofn
 * @version 1.0 Created at: 2018/8/3
 */
@Getter
public enum TaskErrorCode implements ErrorCode {

    TASK_NOT_EXISTS(1, "任务不存在");

    private final int nodeNum;
    private final String msg;

    TaskErrorCode(int nodeNum, String msg) {
        this.nodeNum = nodeNum;
        this.msg = msg;
        ErrorManager.register(AppBootProjectModule.TASK, this);
    }

}
