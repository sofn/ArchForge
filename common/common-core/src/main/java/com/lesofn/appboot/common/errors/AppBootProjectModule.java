package com.lesofn.appboot.common.errors;

import com.lesofn.appboot.common.error.api.ProjectModule;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 18:05
 */
@Getter
@AllArgsConstructor
public enum AppBootProjectModule implements ProjectModule {

    ADMIN_AUTH("AppBoot-Admin", 1, "后台认证", 1),
    ADMIN_USER("AppBoot-Admin", 1, "后台用户", 2),
    TASK("AppBoot-Admin", 1, "后台Task示例", 3);

    final String projectName;
    final int projectCode;
    final String moduleName;
    final int moduleCode;
}
