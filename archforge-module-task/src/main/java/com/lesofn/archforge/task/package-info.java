@NullMarked
@ApplicationModule(id = "task", type = ApplicationModule.Type.CLOSED, allowedDependencies = {
        "common", "infrastructure"
})
package com.lesofn.archforge.task;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
