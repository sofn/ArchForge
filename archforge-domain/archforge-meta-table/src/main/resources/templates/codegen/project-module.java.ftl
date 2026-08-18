package ${packageBase}.error;

import com.lesofn.archforge.common.error.api.ProjectModule;
import lombok.Getter;

@Getter
public enum ${entityName}ProjectModule implements ProjectModule {
    INSTANCE("ArchForge-Admin", 1, "${tableName}", ${moduleCode?c});

    private final String projectName;
    private final int projectCode;
    private final String moduleName;
    private final int moduleCode;

    ${entityName}ProjectModule(String projectName, int projectCode, String moduleName, int moduleCode) {
        this.projectName = projectName;
        this.projectCode = projectCode;
        this.moduleName = moduleName;
        this.moduleCode = moduleCode;
    }
}
