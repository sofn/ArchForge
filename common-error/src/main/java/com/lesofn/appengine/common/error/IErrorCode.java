package com.lesofn.appengine.common.error;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 16:21
 */
public interface IErrorCode {

    /**
     * 最细粒度 Code
     */
    int getNodeNum();

    /**
     * 异常信息 英文
     */
    String getMsg();

    /**
     * 拼接project、module、node后的完整的错误码
     */
    default int getCode() {
        return ErrorManager.genCode(this);
    }

    default IProjectModule projectModule(){
        return ErrorManager.projectModule(this);
    }
}
