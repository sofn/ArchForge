package com.lesofn.archforge.common.error.api;

import com.google.common.base.Preconditions;
import com.lesofn.archforge.common.error.system.SystemProjectModule;

/**
 * 项目和模块的编码
 *
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 16:21
 */
public interface ProjectModule {

    /** 项目编码 */
    int getProjectCode();

    /** 模块编码 */
    int getModuleCode();

    /** 项目名称 */
    String getProjectName();

    /** 模块名称 */
    String getModuleName();

    @SuppressWarnings("ReferenceEquality") // INSTANCE 是标记单例、required/input 为注册枚举 —— 恒等语义即所需语义
    static void check(ProjectModule required, ProjectModule input) {
        Preconditions.checkNotNull(required);
        if (input != SystemProjectModule.INSTANCE) {
            Preconditions.checkState(
                    required == input,
                    "module not match, need: %s-%s(%s-%s)" + " but input: %s-%s(%s-%s)", required.getProjectName(), required
                            .getModuleName(), required
                                    .getProjectCode(), required.getModuleCode(), input.getProjectName(), input.getModuleName(),
                    input.getProjectCode(), input.getModuleCode());
        }
    }
}
