@NullMarked
@ApplicationModule(id = "admin-user-api", type = ApplicationModule.Type.OPEN, allowedDependencies = {
        "common", "infrastructure"
})
package com.lesofn.archforge.user.api;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
