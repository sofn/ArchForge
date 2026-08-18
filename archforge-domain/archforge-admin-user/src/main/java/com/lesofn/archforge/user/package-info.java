@NullMarked
@ApplicationModule(id = "admin-user", type = ApplicationModule.Type.OPEN, allowedDependencies = {
        "common", "infrastructure"
})
package com.lesofn.archforge.user;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
