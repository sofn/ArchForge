@NullMarked
@ApplicationModule(id = "server-admin", type = ApplicationModule.Type.OPEN, allowedDependencies = {
        "common", "infrastructure", "admin-user", "example-task", "meta-table"
})
package com.lesofn.archforge.server.admin;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
