@NullMarked
@ApplicationModule(id = "server-admin", type = ApplicationModule.Type.CLOSED, allowedDependencies = {
        "common", "infrastructure", "admin-user::*", "meta-table::*", "cms::*", "task::*"
})
package com.lesofn.archforge.server.admin;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
