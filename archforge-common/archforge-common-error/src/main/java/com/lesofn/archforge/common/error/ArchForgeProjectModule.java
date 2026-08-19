package com.lesofn.archforge.common.error;

import com.lesofn.archforge.common.error.api.ProjectModule;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArchForgeProjectModule implements ProjectModule {
    ADMIN_AUTH("ArchForge-Admin", 1, "后台认证", 1),
    ADMIN_USER("ArchForge-Admin", 1, "后台用户", 2),
    TASK("ArchForge-Admin", 1, "后台Task示例", 3),
    META_TABLE("ArchForge-Admin", 1, "元表格", 4),
    BLOG("ArchForge-Web", 1, "博客", 5),
    WEB_AUTH("ArchForge-Web", 1, "Web认证", 6),
    CHAT_AI("ArchForge-Admin", 1, "ChatAI", 7);

    final String projectName;
    final int projectCode;
    final String moduleName;
    final int moduleCode;
}
