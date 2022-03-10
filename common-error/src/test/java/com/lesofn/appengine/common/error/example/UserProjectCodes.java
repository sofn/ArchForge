package com.lesofn.appengine.common.error.example;

import com.lesofn.appengine.common.error.IProjectModule;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lishaofeng
 * @version 1.0 Created at: 2022-03-09 16:27
 */
@Getter
@AllArgsConstructor
public enum UserProjectCodes implements IProjectModule {

    LOGIN(1, 1, "登录模块");

    private int projectCode;
    private int moduleCode;
    private String moduleName;

    @Override
    public String getProjectName() {
        return "用户";
    }
}
