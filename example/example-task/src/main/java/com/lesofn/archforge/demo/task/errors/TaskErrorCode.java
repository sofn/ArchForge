package com.lesofn.archforge.demo.task.errors;

import com.lesofn.archforge.common.error.api.ErrorCode;
import com.lesofn.archforge.common.error.manager.ErrorManager;
import com.lesofn.archforge.common.error.ArchForgeProjectModule;
import lombok.Getter;

/**
 * 基础错误码定义
 *
 * @author sofn
 * @version 1.0 Created at: 2018/8/3
 */
@Getter
public enum TaskErrorCode implements ErrorCode {
    TASK_NOT_EXISTS(1, "任务不存在"),
    TASK_STATUS_TRANSITION_INVALID(2, "任务状态从{0}转换到{1}非法"),
    TASK_ALREADY_DONE(3, "任务已完成或已取消，无法执行该操作");

    private final int nodeNum;
    private final String msg;

    TaskErrorCode(int nodeNum, String msg) {
        this.nodeNum = nodeNum;
        this.msg = msg;
        ErrorManager.register(ArchForgeProjectModule.TASK, this);
    }
}
