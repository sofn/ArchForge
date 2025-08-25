package com.lesofn.matrixboot.common.errors;

import com.lesofn.matrixboot.common.error.api.ProjectModule;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-09 18:05
 */
@Getter
@AllArgsConstructor
public enum MaxtrixBootProjectModule implements ProjectModule {

    USER("MatrixBoot", 1, "用户模块", 1);

    final String projectName;
    final int projectCode;
    final String moduleName;
    final int moduleCode;
}
