@NullMarked
@ApplicationModule(id = "admin-user", type = ApplicationModule.Type.OPEN, allowedDependencies = {
        "common", "infrastructure", "admin-user-api"
})
package com.lesofn.archforge.user.internal;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
