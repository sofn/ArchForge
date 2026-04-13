package com.lesofn.appforge.demo.task.errors;

import com.lesofn.appforge.common.error.api.ErrorCode;
import com.lesofn.appforge.common.error.manager.ErrorManager;
import com.lesofn.appforge.common.errors.AppForgeProjectModule;
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
        ErrorManager.register(AppForgeProjectModule.TASK, this);
    }
}
