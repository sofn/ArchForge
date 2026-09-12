package com.lesofn.archforge.common.error.system;

import com.lesofn.archforge.common.error.api.ProjectModule;

/**
 * 系统级错误码所属的项目模块标识。
 *
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 18:05
 */
public class SystemProjectModule implements ProjectModule {

    public static final SystemProjectModule INSTANCE = new SystemProjectModule();

    @Override
    public int getProjectCode() { return 0; }

    @Override
    public int getModuleCode() { return 0; }

    @Override
    public String getProjectName() { return "default"; }

    @Override
    public String getModuleName() { return "default"; }
}
