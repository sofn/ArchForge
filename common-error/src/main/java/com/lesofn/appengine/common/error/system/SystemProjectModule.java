package com.lesofn.appengine.common.error.system;

import com.lesofn.appengine.common.error.IProjectModule;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 18:05
 */
public class SystemProjectModule implements IProjectModule {

    public static final SystemProjectModule INSTANCE = new SystemProjectModule();

    @Override
    public int getProjectCode() {
        return 0;
    }

    @Override
    public int getModuleCode() {
        return 0;
    }

    @Override
    public String getProjectName() {
        return "default";
    }

    @Override
    public String getModuleName() {
        return "default";
    }
}
