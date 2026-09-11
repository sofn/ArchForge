@NullMarked
// 保持 OPEN：infrastructure 无 api/internal 划分，整体充当平台层；
// 待其完成 api/internal 重组后再翻 CLOSED
@ApplicationModule(id = "infrastructure", type = ApplicationModule.Type.OPEN, allowedDependencies = "common")
package com.lesofn.archforge.infrastructure;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
